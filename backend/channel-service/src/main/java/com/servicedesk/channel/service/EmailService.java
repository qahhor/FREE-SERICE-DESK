package com.servicedesk.channel.service;

import com.servicedesk.channel.dto.SendEmailRequest;
import com.servicedesk.channel.entity.EmailConfiguration;
import com.servicedesk.channel.entity.EmailMessage;
import com.servicedesk.channel.repository.EmailConfigurationRepository;
import com.servicedesk.channel.repository.EmailMessageRepository;
import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.common.exception.ServiceDeskException;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final EmailConfigurationRepository configRepository;
    private final EmailMessageRepository messageRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String TICKET_QUEUE = "ticket.email.inbound";
    private static final int MAX_RETRY_COUNT = 3;

    @Transactional
    public EmailMessage sendEmail(SendEmailRequest request) {
        EmailConfiguration config = configRepository.findByChannelId(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Email configuration not found"));

        if (!config.getEnabled()) {
            throw new ServiceDeskException("Email channel is disabled");
        }

        EmailMessage message = EmailMessage.builder()
                .channelId(request.getChannelId())
                .ticketId(request.getTicketId())
                .direction(EmailMessage.Direction.OUTBOUND)
                .fromAddress(config.getEmailAddress())
                .fromName(config.getFromName())
                .toAddresses(String.join(",", request.getTo()))
                .ccAddresses(request.getCc() != null ? String.join(",", request.getCc()) : null)
                .bccAddresses(request.getBcc() != null ? String.join(",", request.getBcc()) : null)
                .subject(request.getSubject())
                .bodyText(request.getBodyText())
                .bodyHtml(request.getBodyHtml())
                .inReplyTo(request.getInReplyTo())
                .hasAttachments(request.getAttachments() != null && !request.getAttachments().isEmpty())
                .attachmentCount(request.getAttachments() != null ? request.getAttachments().size() : 0)
                .status(EmailMessage.Status.PENDING)
                .build();

        message = messageRepository.save(message);

        // Send asynchronously
        sendEmailAsync(message, config, request);

        return message;
    }

    @Async
    public void sendEmailAsync(EmailMessage message, EmailConfiguration config, SendEmailRequest request) {
        try {
            Session session = createSmtpSession(config);
            MimeMessage mimeMessage = createMimeMessage(session, config, request);

            Transport.send(mimeMessage);

            message.setStatus(EmailMessage.Status.SENT);
            message.setSentAt(LocalDateTime.now());
            message.setMessageId(mimeMessage.getMessageID());
            messageRepository.save(message);

            log.info("Email sent successfully: {}", message.getId());

        } catch (Exception e) {
            log.error("Failed to send email: {}", message.getId(), e);
            handleSendFailure(message, e);
        }
    }

    private Session createSmtpSession(EmailConfiguration config) {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        props.put("mail.smtp.auth", config.getSmtpAuth());

        if (config.getSmtpTls()) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (config.getSmtpSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
            }
        });
    }

    private MimeMessage createMimeMessage(Session session, EmailConfiguration config, SendEmailRequest request)
            throws MessagingException {
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(config.getEmailAddress(), config.getFromName(), "UTF-8"));

        for (String to : request.getTo()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }

        if (request.getCc() != null) {
            for (String cc : request.getCc()) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }
        }

        if (request.getBcc() != null) {
            for (String bcc : request.getBcc()) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
            }
        }

        message.setSubject(request.getSubject(), "UTF-8");

        if (request.getInReplyTo() != null) {
            message.setHeader("In-Reply-To", request.getInReplyTo());
            message.setHeader("References", request.getInReplyTo());
        }

        if (config.getReplyTo() != null) {
            message.setReplyTo(new Address[]{new InternetAddress(config.getReplyTo())});
        }

        // Create message body
        if (request.getBodyHtml() != null && request.getBodyText() != null) {
            MimeMultipart multipart = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(request.getBodyText(), "UTF-8");
            multipart.addBodyPart(textPart);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(request.getBodyHtml(), "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
        } else if (request.getBodyHtml() != null) {
            message.setContent(request.getBodyHtml(), "text/html; charset=UTF-8");
        } else {
            message.setText(request.getBodyText() != null ? request.getBodyText() : "", "UTF-8");
        }

        message.setSentDate(new Date());

        return message;
    }

    private void handleSendFailure(EmailMessage message, Exception e) {
        message.setRetryCount(message.getRetryCount() + 1);
        message.setErrorMessage(e.getMessage());

        if (message.getRetryCount() >= MAX_RETRY_COUNT) {
            message.setStatus(EmailMessage.Status.FAILED);
        }

        messageRepository.save(message);
    }

    public void fetchEmails(EmailConfiguration config) {
        if (!config.getEnabled() || config.getImapHost() == null) {
            return;
        }

        try {
            Session session = createImapSession(config);
            Store store = session.getStore("imaps");
            store.connect(config.getImapHost(), config.getImapUsername(), config.getImapPassword());

            Folder inbox = store.getFolder(config.getImapFolder());
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message message : messages) {
                try {
                    processInboundEmail(message, config);
                    message.setFlag(Flags.Flag.SEEN, true);
                } catch (Exception e) {
                    log.error("Failed to process email: {}", e.getMessage());
                }
            }

            inbox.close(false);
            store.close();

            config.setLastPollAt(LocalDateTime.now());
            config.setLastError(null);
            configRepository.save(config);

        } catch (Exception e) {
            log.error("Failed to fetch emails from {}: {}", config.getEmailAddress(), e.getMessage());
            config.setLastError(e.getMessage());
            configRepository.save(config);
        }
    }

    private Session createImapSession(EmailConfiguration config) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", config.getImapHost());
        props.put("mail.imaps.port", config.getImapPort());
        props.put("mail.imaps.ssl.enable", config.getImapSsl());

        return Session.getInstance(props);
    }

    private void processInboundEmail(Message message, EmailConfiguration config)
            throws MessagingException, IOException {
        String messageId = message.getHeader("Message-ID") != null ?
                message.getHeader("Message-ID")[0] : UUID.randomUUID().toString();

        if (messageRepository.existsByMessageId(messageId)) {
            log.debug("Email already processed: {}", messageId);
            return;
        }

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        String inReplyTo = message.getHeader("In-Reply-To") != null ?
                message.getHeader("In-Reply-To")[0] : null;

        EmailMessage emailMessage = EmailMessage.builder()
                .messageId(messageId)
                .channelId(config.getChannelId())
                .direction(EmailMessage.Direction.INBOUND)
                .fromAddress(from.getAddress())
                .fromName(from.getPersonal())
                .toAddresses(getRecipients(message, Message.RecipientType.TO))
                .ccAddresses(getRecipients(message, Message.RecipientType.CC))
                .subject(message.getSubject())
                .bodyText(getTextContent(message))
                .bodyHtml(getHtmlContent(message))
                .inReplyTo(inReplyTo)
                .hasAttachments(hasAttachments(message))
                .status(EmailMessage.Status.PROCESSING)
                .receivedAt(message.getReceivedDate() != null ?
                        LocalDateTime.ofInstant(message.getReceivedDate().toInstant(),
                                java.time.ZoneId.systemDefault()) : LocalDateTime.now())
                .build();

        emailMessage = messageRepository.save(emailMessage);

        // Send to ticket processing queue
        Map<String, Object> event = new HashMap<>();
        event.put("emailMessageId", emailMessage.getId());
        event.put("channelId", config.getChannelId());
        event.put("fromAddress", from.getAddress());
        event.put("subject", message.getSubject());
        event.put("inReplyTo", inReplyTo);
        event.put("autoCreateTicket", config.getAutoCreateTicket());
        event.put("defaultPriority", config.getDefaultPriority());
        event.put("defaultTeamId", config.getDefaultTeamId());

        rabbitTemplate.convertAndSend(TICKET_QUEUE, event);

        log.info("Inbound email processed: {} from {}", emailMessage.getId(), from.getAddress());
    }

    private String getRecipients(Message message, Message.RecipientType type) throws MessagingException {
        Address[] addresses = message.getRecipients(type);
        if (addresses == null) return null;

        List<String> emails = new ArrayList<>();
        for (Address address : addresses) {
            if (address instanceof InternetAddress) {
                emails.add(((InternetAddress) address).getAddress());
            }
        }
        return String.join(",", emails);
    }

    private String getTextContent(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            return getTextFromMultipart((Multipart) message.getContent());
        }
        return null;
    }

    private String getTextFromMultipart(Multipart multipart) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof Multipart) {
                String result = getTextFromMultipart((Multipart) bodyPart.getContent());
                if (result != null) return result;
            }
        }
        return null;
    }

    private String getHtmlContent(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            return getHtmlFromMultipart((Multipart) message.getContent());
        }
        return null;
    }

    private String getHtmlFromMultipart(Multipart multipart) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof Multipart) {
                String result = getHtmlFromMultipart((Multipart) bodyPart.getContent());
                if (result != null) return result;
            }
        }
        return null;
    }

    private boolean hasAttachments(Message message) throws MessagingException, IOException {
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<EmailMessage> getEmailsByTicket(String ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    /**
     * Test IMAP and SMTP connections for the given email configuration
     * @param config the email configuration to test
     * @return test result with detailed information about both connections
     */
    public com.servicedesk.channel.dto.EmailConnectionTestResult testConnection(EmailConfiguration config) {
        boolean imapSuccess = false;
        boolean smtpSuccess = false;
        String imapMessage = "Not tested";
        String smtpMessage = "Not tested";
        long imapResponseTimeMs = 0;
        long smtpResponseTimeMs = 0;

        // Test IMAP connection
        if (config.getImapHost() != null && !config.getImapHost().isEmpty()) {
            long startTime = System.currentTimeMillis();
            try {
                testImapConnection(config);
                imapSuccess = true;
                imapResponseTimeMs = System.currentTimeMillis() - startTime;
                imapMessage = "IMAP connection successful";
                log.info("IMAP connection test successful for {}", config.getEmailAddress());
            } catch (Exception e) {
                imapResponseTimeMs = System.currentTimeMillis() - startTime;
                imapMessage = "IMAP connection failed: " + e.getMessage();
                log.warn("IMAP connection test failed for {}: {}", config.getEmailAddress(), e.getMessage());
            }
        } else {
            imapMessage = "IMAP not configured";
        }

        // Test SMTP connection
        if (config.getSmtpHost() != null && !config.getSmtpHost().isEmpty()) {
            long startTime = System.currentTimeMillis();
            try {
                testSmtpConnection(config);
                smtpSuccess = true;
                smtpResponseTimeMs = System.currentTimeMillis() - startTime;
                smtpMessage = "SMTP connection successful";
                log.info("SMTP connection test successful for {}", config.getEmailAddress());
            } catch (Exception e) {
                smtpResponseTimeMs = System.currentTimeMillis() - startTime;
                smtpMessage = "SMTP connection failed: " + e.getMessage();
                log.warn("SMTP connection test failed for {}: {}", config.getEmailAddress(), e.getMessage());
            }
        } else {
            smtpMessage = "SMTP not configured";
        }

        return com.servicedesk.channel.dto.EmailConnectionTestResult.partial(
                imapSuccess, imapMessage, imapResponseTimeMs,
                smtpSuccess, smtpMessage, smtpResponseTimeMs
        );
    }

    /**
     * Test IMAP connection
     */
    private void testImapConnection(EmailConfiguration config) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", config.getImapSsl() ? "imaps" : "imap");
        props.put("mail.imaps.host", config.getImapHost());
        props.put("mail.imaps.port", config.getImapPort());
        props.put("mail.imaps.ssl.enable", config.getImapSsl());
        props.put("mail.imaps.connectiontimeout", "10000"); // 10 seconds timeout
        props.put("mail.imaps.timeout", "10000");
        props.put("mail.imap.connectiontimeout", "10000");
        props.put("mail.imap.timeout", "10000");

        Session session = Session.getInstance(props);
        Store store = null;
        try {
            store = session.getStore(config.getImapSsl() ? "imaps" : "imap");
            store.connect(config.getImapHost(), config.getImapPort(), 
                    config.getImapUsername(), config.getImapPassword());
            
            // Try to open the configured folder to verify access
            Folder folder = store.getFolder(config.getImapFolder() != null ? config.getImapFolder() : "INBOX");
            folder.open(Folder.READ_ONLY);
            folder.close(false);
        } finally {
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }

    /**
     * Test SMTP connection
     */
    private void testSmtpConnection(EmailConfiguration config) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        props.put("mail.smtp.auth", config.getSmtpAuth());
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds timeout
        props.put("mail.smtp.timeout", "10000");

        if (config.getSmtpTls()) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (config.getSmtpSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
            }
        });

        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.connect(config.getSmtpHost(), config.getSmtpPort(),
                    config.getSmtpUsername(), config.getSmtpPassword());
        } finally {
            if (transport != null && transport.isConnected()) {
                transport.close();
            }
        }
    }
}
