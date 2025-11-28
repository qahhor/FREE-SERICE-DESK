package com.servicedesk.notification.service;

import com.servicedesk.notification.client.UserServiceClient;
import com.servicedesk.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserServiceClient userServiceClient;

    @Value("${servicedesk.notification.email.from:noreply@servicedesk.local}")
    private String fromEmail;

    @Value("${servicedesk.notification.email.from-name:Service Desk}")
    private String fromName;

    @Value("${servicedesk.notification.email.retry-count:3}")
    private int retryCount;

    @Value("${servicedesk.notification.email.retry-delay:5000}")
    private long retryDelay;

    @Async
    public void sendNotificationEmail(Notification notification) {
        String userEmail = resolveUserEmail(notification);

        if (userEmail == null || userEmail.isBlank()) {
            log.warn("No email found for user: {}, skipping email notification", notification.getUserId());
            return;
        }

        int attempts = 0;
        Exception lastException = null;

        while (attempts < retryCount) {
            try {
                sendEmail(notification, userEmail);
                log.info("Notification email sent to: {} (attempt {})", userEmail, attempts + 1);
                return;
            } catch (Exception e) {
                attempts++;
                lastException = e;
                log.warn("Failed to send email to {} (attempt {}): {}", userEmail, attempts, e.getMessage());

                if (attempts < retryCount) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Failed to send notification email to {} after {} attempts", userEmail, retryCount, lastException);
    }

    private void sendEmail(Notification notification, String userEmail) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("title", notification.getTitle());
        context.setVariable("message", notification.getMessage());
        context.setVariable("actionUrl", notification.getActionUrl());
        context.setVariable("type", notification.getType().name());
        context.setVariable("priority", notification.getPriority().name());

        String htmlContent = templateEngine.process("notification-email", context);

        helper.setFrom(fromEmail, fromName);
        helper.setTo(userEmail);
        helper.setSubject("[ServiceDesk] " + notification.getTitle());
        helper.setText(htmlContent, true);

        // Set priority header for urgent notifications
        if (notification.getPriority() == Notification.Priority.URGENT ||
            notification.getPriority() == Notification.Priority.HIGH) {
            message.setHeader("X-Priority", "1");
            message.setHeader("X-MSMail-Priority", "High");
        }

        mailSender.send(message);
    }

    private String resolveUserEmail(Notification notification) {
        // First, try to get email directly from notification (preferred)
        if (notification.getUserEmail() != null && !notification.getUserEmail().isBlank()) {
            return notification.getUserEmail();
        }

        // Fallback: Call user service to get email
        return fetchUserEmailFromService(notification.getUserId());
    }

    private String fetchUserEmailFromService(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        try {
            var user = userServiceClient.getUserById(userId);
            if (user != null && user.email() != null) {
                log.debug("Fetched email {} for user {} from user service", user.email(), userId);
                return user.email();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user email from user service for userId {}: {}", userId, e.getMessage());
        }

        return null;
    }
}
