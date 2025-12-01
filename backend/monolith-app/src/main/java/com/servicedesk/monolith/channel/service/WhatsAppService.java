package com.servicedesk.monolith.channel.service;

import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.monolith.channel.dto.WhatsAppConfigurationDto;
import com.servicedesk.monolith.channel.dto.WhatsAppMessageDto;
import com.servicedesk.monolith.channel.dto.WhatsAppWebhookDto;
import com.servicedesk.monolith.channel.entity.WhatsAppConfiguration;
import com.servicedesk.monolith.channel.entity.WhatsAppContact;
import com.servicedesk.monolith.channel.entity.WhatsAppMessage;
import com.servicedesk.monolith.channel.repository.WhatsAppConfigurationRepository;
import com.servicedesk.monolith.channel.repository.WhatsAppContactRepository;
import com.servicedesk.monolith.channel.repository.WhatsAppMessageRepository;
import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.common.exception.ServiceDeskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppConfigurationRepository configRepository;
    private final WhatsAppMessageRepository messageRepository;
    private final WhatsAppContactRepository contactRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String WHATSAPP_API_URL = "https://graph.facebook.com/";
    private static final String TICKET_QUEUE = "ticket.whatsapp.inbound";

    // ==================== Configuration Management ====================

    @Transactional
    public WhatsAppConfigurationDto createConfiguration(WhatsAppConfigurationDto.CreateRequest request) {
        if (configRepository.existsByPhoneNumberId(request.getPhoneNumberId())) {
            throw new ServiceDeskException("WhatsApp configuration already exists for this phone number");
        }

        WhatsAppConfiguration config = WhatsAppConfiguration.builder()
                .channelId(request.getChannelId())
                .name(request.getName())
                .phoneNumberId(request.getPhoneNumberId())
                .businessAccountId(request.getBusinessAccountId())
                .accessToken(request.getAccessToken())
                .verifyToken(request.getVerifyToken())
                .appSecret(request.getAppSecret())
                .displayPhoneNumber(request.getDisplayPhoneNumber())
                .welcomeMessage(request.getWelcomeMessage())
                .autoCreateTicket(request.getAutoCreateTicket() != null ? request.getAutoCreateTicket() : true)
                .defaultPriority(request.getDefaultPriority() != null ? request.getDefaultPriority() : "MEDIUM")
                .defaultTeamId(request.getDefaultTeamId())
                .defaultCategoryId(request.getDefaultCategoryId())
                .apiVersion(request.getApiVersion() != null ? request.getApiVersion() : WhatsAppConfiguration.ApiVersion.V18_0)
                .enabled(true)
                .build();

        config = configRepository.save(config);
        log.info("WhatsApp configuration created: {} for channel {}", config.getId(), config.getChannelId());

        return toConfigDto(config);
    }

    @Transactional
    public WhatsAppConfigurationDto updateConfiguration(String configId, WhatsAppConfigurationDto.UpdateRequest request) {
        WhatsAppConfiguration config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));

        if (request.getName() != null) config.setName(request.getName());
        if (request.getAccessToken() != null) config.setAccessToken(request.getAccessToken());
        if (request.getVerifyToken() != null) config.setVerifyToken(request.getVerifyToken());
        if (request.getAppSecret() != null) config.setAppSecret(request.getAppSecret());
        if (request.getDisplayPhoneNumber() != null) config.setDisplayPhoneNumber(request.getDisplayPhoneNumber());
        if (request.getWelcomeMessage() != null) config.setWelcomeMessage(request.getWelcomeMessage());
        if (request.getAutoCreateTicket() != null) config.setAutoCreateTicket(request.getAutoCreateTicket());
        if (request.getDefaultPriority() != null) config.setDefaultPriority(request.getDefaultPriority());
        if (request.getDefaultTeamId() != null) config.setDefaultTeamId(request.getDefaultTeamId());
        if (request.getDefaultCategoryId() != null) config.setDefaultCategoryId(request.getDefaultCategoryId());
        if (request.getEnabled() != null) config.setEnabled(request.getEnabled());
        if (request.getApiVersion() != null) config.setApiVersion(request.getApiVersion());

        config = configRepository.save(config);
        return toConfigDto(config);
    }

    public WhatsAppConfigurationDto getConfiguration(String configId) {
        return configRepository.findById(configId)
                .map(this::toConfigDto)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));
    }

    public WhatsAppConfigurationDto getConfigurationByChannel(String channelId) {
        return configRepository.findByChannelId(channelId)
                .map(this::toConfigDto)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found for channel"));
    }

    // ==================== Webhook Handling ====================

    public boolean verifyWebhook(String phoneNumberId, String mode, String token, String challenge) {
        WhatsAppConfiguration config = configRepository.findByPhoneNumberId(phoneNumberId)
                .orElse(null);

        if (config == null) {
            log.warn("No configuration found for phone number ID: {}", phoneNumberId);
            return false;
        }

        if ("subscribe".equals(mode) && config.getVerifyToken().equals(token)) {
            log.info("Webhook verified for phone number ID: {}", phoneNumberId);
            return true;
        }

        log.warn("Webhook verification failed for phone number ID: {}", phoneNumberId);
        return false;
    }

    @Transactional
    public void processWebhook(WhatsAppWebhookDto webhook) {
        if (webhook.getEntry() == null) {
            return;
        }

        for (WhatsAppWebhookDto.Entry entry : webhook.getEntry()) {
            if (entry.getChanges() == null) continue;

            for (WhatsAppWebhookDto.Change change : entry.getChanges()) {
                if (change.getValue() == null) continue;

                WhatsAppWebhookDto.Value value = change.getValue();
                String phoneNumberId = value.getMetadata() != null ? value.getMetadata().getPhoneNumberId() : null;

                if (phoneNumberId == null) continue;

                WhatsAppConfiguration config = configRepository.findByPhoneNumberId(phoneNumberId).orElse(null);
                if (config == null || !config.getEnabled()) {
                    log.warn("Ignoring webhook for unconfigured/disabled phone: {}", phoneNumberId);
                    continue;
                }

                // Process messages
                if (value.getMessages() != null) {
                    for (WhatsAppWebhookDto.Message msg : value.getMessages()) {
                        processInboundMessage(msg, value.getContacts(), config);
                    }
                }

                // Process status updates
                if (value.getStatuses() != null) {
                    for (WhatsAppWebhookDto.Status status : value.getStatuses()) {
                        processStatusUpdate(status, config);
                    }
                }
            }
        }
    }

    private void processInboundMessage(WhatsAppWebhookDto.Message msg,
                                        List<WhatsAppWebhookDto.Contact> contacts,
                                        WhatsAppConfiguration config) {
        // Check if message already processed
        if (messageRepository.existsByWhatsappMessageId(msg.getId())) {
            log.debug("Message already processed: {}", msg.getId());
            return;
        }

        String profileName = null;
        if (contacts != null && !contacts.isEmpty()) {
            profileName = contacts.get(0).getProfile() != null ?
                    contacts.get(0).getProfile().getName() : null;
        }

        // Determine message type and extract content
        WhatsAppMessage.MessageType messageType = determineMessageType(msg.getType());
        String text = null;
        String caption = null;
        String mediaId = null;
        String mediaMimeType = null;
        String mediaSha256 = null;
        String mediaFilename = null;
        Double latitude = null;
        Double longitude = null;
        String locationName = null;
        String locationAddress = null;

        switch (messageType) {
            case TEXT:
                text = msg.getText() != null ? msg.getText().getBody() : null;
                break;
            case IMAGE:
                if (msg.getImage() != null) {
                    caption = msg.getImage().getCaption();
                    mediaId = msg.getImage().getId();
                    mediaMimeType = msg.getImage().getMimeType();
                    mediaSha256 = msg.getImage().getSha256();
                }
                break;
            case AUDIO:
                if (msg.getAudio() != null) {
                    mediaId = msg.getAudio().getId();
                    mediaMimeType = msg.getAudio().getMimeType();
                    mediaSha256 = msg.getAudio().getSha256();
                }
                break;
            case VIDEO:
                if (msg.getVideo() != null) {
                    caption = msg.getVideo().getCaption();
                    mediaId = msg.getVideo().getId();
                    mediaMimeType = msg.getVideo().getMimeType();
                    mediaSha256 = msg.getVideo().getSha256();
                }
                break;
            case DOCUMENT:
                if (msg.getDocument() != null) {
                    caption = msg.getDocument().getCaption();
                    mediaId = msg.getDocument().getId();
                    mediaMimeType = msg.getDocument().getMimeType();
                    mediaSha256 = msg.getDocument().getSha256();
                    mediaFilename = msg.getDocument().getFilename();
                }
                break;
            case LOCATION:
                if (msg.getLocation() != null) {
                    latitude = msg.getLocation().getLatitude();
                    longitude = msg.getLocation().getLongitude();
                    locationName = msg.getLocation().getName();
                    locationAddress = msg.getLocation().getAddress();
                }
                break;
            case INTERACTIVE:
                if (msg.getInteractive() != null) {
                    if (msg.getInteractive().getButtonReply() != null) {
                        text = msg.getInteractive().getButtonReply().getTitle();
                    } else if (msg.getInteractive().getListReply() != null) {
                        text = msg.getInteractive().getListReply().getTitle();
                    }
                }
                break;
            case BUTTON:
                if (msg.getButton() != null) {
                    text = msg.getButton().getText();
                }
                break;
            default:
                text = "[Unsupported message type: " + msg.getType() + "]";
        }

        // Create message entity
        WhatsAppMessage message = WhatsAppMessage.builder()
                .whatsappMessageId(msg.getId())
                .channelId(config.getChannelId())
                .fromNumber(msg.getFrom())
                .toNumber(config.getDisplayPhoneNumber())
                .profileName(profileName)
                .direction(WhatsAppMessage.Direction.INBOUND)
                .messageType(messageType)
                .text(text)
                .caption(caption)
                .mediaId(mediaId)
                .mediaMimeType(mediaMimeType)
                .mediaSha256(mediaSha256)
                .mediaFilename(mediaFilename)
                .latitude(latitude)
                .longitude(longitude)
                .locationName(locationName)
                .locationAddress(locationAddress)
                .status(WhatsAppMessage.Status.DELIVERED)
                .build();

        // Handle context (reply)
        if (msg.getContext() != null) {
            message.setContextMessageId(msg.getContext().getId());
            message.setContextFrom(msg.getContext().getFrom());
        }

        message = messageRepository.save(message);

        // Update or create contact
        updateContact(config.getChannelId(), msg.getFrom(), profileName);

        // Send to ticket processing queue
        Map<String, Object> event = new HashMap<>();
        event.put("whatsappMessageId", message.getId());
        event.put("channelId", config.getChannelId());
        event.put("fromNumber", msg.getFrom());
        event.put("profileName", profileName);
        event.put("text", text != null ? text : caption);
        event.put("messageType", messageType.name());
        event.put("autoCreateTicket", config.getAutoCreateTicket());
        event.put("defaultPriority", config.getDefaultPriority());
        event.put("defaultTeamId", config.getDefaultTeamId());

        eventPublisher.convertAndSend(TICKET_QUEUE, event);

        log.info("WhatsApp message processed: {} from {}", message.getId(), msg.getFrom());
    }

    private void processStatusUpdate(WhatsAppWebhookDto.Status status, WhatsAppConfiguration config) {
        WhatsAppMessage message = messageRepository.findByWhatsappMessageId(status.getId()).orElse(null);

        if (message == null) {
            log.debug("Message not found for status update: {}", status.getId());
            return;
        }

        LocalDateTime timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(Long.parseLong(status.getTimestamp())),
                ZoneId.systemDefault());

        switch (status.getStatus()) {
            case "sent":
                message.setStatus(WhatsAppMessage.Status.SENT);
                message.setSentAt(timestamp);
                break;
            case "delivered":
                message.setStatus(WhatsAppMessage.Status.DELIVERED);
                message.setDeliveredAt(timestamp);
                break;
            case "read":
                message.setStatus(WhatsAppMessage.Status.READ);
                message.setReadAt(timestamp);
                break;
            case "failed":
                message.setStatus(WhatsAppMessage.Status.FAILED);
                message.setFailedAt(timestamp);
                if (status.getErrors() != null && !status.getErrors().isEmpty()) {
                    WhatsAppWebhookDto.Error error = status.getErrors().get(0);
                    message.setErrorCode(String.valueOf(error.getCode()));
                    message.setErrorMessage(error.getMessage());
                }
                break;
        }

        messageRepository.save(message);
        log.debug("Status updated for message {}: {}", status.getId(), status.getStatus());
    }

    private void updateContact(String channelId, String phoneNumber, String profileName) {
        WhatsAppContact contact = contactRepository.findByChannelIdAndPhoneNumber(channelId, phoneNumber)
                .orElse(WhatsAppContact.builder()
                        .channelId(channelId)
                        .waId(phoneNumber)
                        .phoneNumber(phoneNumber)
                        .firstMessageAt(LocalDateTime.now())
                        .messageCount(0)
                        .build());

        contact.setProfileName(profileName);
        contact.setLastMessageAt(LocalDateTime.now());
        contact.setMessageCount(contact.getMessageCount() + 1);

        contactRepository.save(contact);
    }

    // ==================== Send Messages ====================

    @Transactional
    public WhatsAppMessageDto sendTextMessage(WhatsAppMessageDto.SendTextRequest request) {
        WhatsAppConfiguration config = configRepository.findByChannelId(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));

        if (!config.getEnabled()) {
            throw new ServiceDeskException("WhatsApp channel is disabled");
        }

        WhatsAppMessage message = WhatsAppMessage.builder()
                .channelId(request.getChannelId())
                .fromNumber(config.getDisplayPhoneNumber())
                .toNumber(request.getToNumber())
                .ticketId(request.getTicketId())
                .direction(WhatsAppMessage.Direction.OUTBOUND)
                .messageType(WhatsAppMessage.MessageType.TEXT)
                .text(request.getText())
                .contextMessageId(request.getContextMessageId())
                .status(WhatsAppMessage.Status.PENDING)
                .build();

        message = messageRepository.save(message);

        sendTextMessageAsync(message, config, request.getPreviewUrl());

        return toMessageDto(message);
    }

    @Async
    public void sendTextMessageAsync(WhatsAppMessage message, WhatsAppConfiguration config, Boolean previewUrl) {
        try {
            String url = buildApiUrl(config, "messages");

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", message.getToNumber());
            body.put("type", "text");

            Map<String, Object> textObj = new HashMap<>();
            textObj.put("body", message.getText());
            if (previewUrl != null) {
                textObj.put("preview_url", previewUrl);
            }
            body.put("text", textObj);

            if (message.getContextMessageId() != null) {
                Map<String, Object> context = new HashMap<>();
                context.put("message_id", message.getContextMessageId());
                body.put("context", context);
            }

            JsonNode response = sendApiRequest(url, config.getAccessToken(), body);

            if (response.has("messages") && response.get("messages").isArray()) {
                String messageId = response.get("messages").get(0).get("id").asText();
                message.setWhatsappMessageId(messageId);
                message.setStatus(WhatsAppMessage.Status.SENT);
                message.setSentAt(LocalDateTime.now());
            }

            messageRepository.save(message);
            log.info("WhatsApp text message sent: {} to {}", message.getId(), message.getToNumber());

        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Transactional
    public WhatsAppMessageDto sendMediaMessage(WhatsAppMessageDto.SendMediaRequest request) {
        WhatsAppConfiguration config = configRepository.findByChannelId(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));

        if (!config.getEnabled()) {
            throw new ServiceDeskException("WhatsApp channel is disabled");
        }

        WhatsAppMessage message = WhatsAppMessage.builder()
                .channelId(request.getChannelId())
                .fromNumber(config.getDisplayPhoneNumber())
                .toNumber(request.getToNumber())
                .ticketId(request.getTicketId())
                .direction(WhatsAppMessage.Direction.OUTBOUND)
                .messageType(request.getMessageType())
                .mediaUrl(request.getMediaUrl())
                .caption(request.getCaption())
                .mediaFilename(request.getFilename())
                .status(WhatsAppMessage.Status.PENDING)
                .build();

        message = messageRepository.save(message);

        sendMediaMessageAsync(message, config);

        return toMessageDto(message);
    }

    @Async
    public void sendMediaMessageAsync(WhatsAppMessage message, WhatsAppConfiguration config) {
        try {
            String url = buildApiUrl(config, "messages");

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", message.getToNumber());

            String mediaType = message.getMessageType().name().toLowerCase();
            body.put("type", mediaType);

            Map<String, Object> mediaObj = new HashMap<>();
            mediaObj.put("link", message.getMediaUrl());
            if (message.getCaption() != null) {
                mediaObj.put("caption", message.getCaption());
            }
            if (message.getMediaFilename() != null && message.getMessageType() == WhatsAppMessage.MessageType.DOCUMENT) {
                mediaObj.put("filename", message.getMediaFilename());
            }
            body.put(mediaType, mediaObj);

            JsonNode response = sendApiRequest(url, config.getAccessToken(), body);

            if (response.has("messages") && response.get("messages").isArray()) {
                String messageId = response.get("messages").get(0).get("id").asText();
                message.setWhatsappMessageId(messageId);
                message.setStatus(WhatsAppMessage.Status.SENT);
                message.setSentAt(LocalDateTime.now());
            }

            messageRepository.save(message);
            log.info("WhatsApp media message sent: {} to {}", message.getId(), message.getToNumber());

        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Transactional
    public WhatsAppMessageDto sendTemplateMessage(WhatsAppMessageDto.SendTemplateRequest request) {
        WhatsAppConfiguration config = configRepository.findByChannelId(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));

        if (!config.getEnabled()) {
            throw new ServiceDeskException("WhatsApp channel is disabled");
        }

        WhatsAppMessage message = WhatsAppMessage.builder()
                .channelId(request.getChannelId())
                .fromNumber(config.getDisplayPhoneNumber())
                .toNumber(request.getToNumber())
                .ticketId(request.getTicketId())
                .direction(WhatsAppMessage.Direction.OUTBOUND)
                .messageType(WhatsAppMessage.MessageType.TEMPLATE)
                .templateName(request.getTemplateName())
                .templateLanguage(request.getTemplateLanguage())
                .status(WhatsAppMessage.Status.PENDING)
                .build();

        message = messageRepository.save(message);

        sendTemplateMessageAsync(message, config, request);

        return toMessageDto(message);
    }

    @Async
    public void sendTemplateMessageAsync(WhatsAppMessage message, WhatsAppConfiguration config,
                                          WhatsAppMessageDto.SendTemplateRequest request) {
        try {
            String url = buildApiUrl(config, "messages");

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", message.getToNumber());
            body.put("type", "template");

            Map<String, Object> template = new HashMap<>();
            template.put("name", request.getTemplateName());

            Map<String, Object> language = new HashMap<>();
            language.put("code", request.getTemplateLanguage() != null ? request.getTemplateLanguage() : "en");
            template.put("language", language);

            // Add components if provided
            List<Map<String, Object>> components = new java.util.ArrayList<>();

            if (request.getHeaderParameters() != null && request.getHeaderParameters().length > 0) {
                Map<String, Object> header = new HashMap<>();
                header.put("type", "header");
                header.put("parameters", buildParameters(request.getHeaderParameters()));
                components.add(header);
            }

            if (request.getBodyParameters() != null && request.getBodyParameters().length > 0) {
                Map<String, Object> bodyComponent = new HashMap<>();
                bodyComponent.put("type", "body");
                bodyComponent.put("parameters", buildParameters(request.getBodyParameters()));
                components.add(bodyComponent);
            }

            if (!components.isEmpty()) {
                template.put("components", components);
            }

            body.put("template", template);

            JsonNode response = sendApiRequest(url, config.getAccessToken(), body);

            if (response.has("messages") && response.get("messages").isArray()) {
                String messageId = response.get("messages").get(0).get("id").asText();
                message.setWhatsappMessageId(messageId);
                message.setStatus(WhatsAppMessage.Status.SENT);
                message.setSentAt(LocalDateTime.now());
            }

            messageRepository.save(message);
            log.info("WhatsApp template message sent: {} to {}", message.getId(), message.getToNumber());

        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Transactional
    public WhatsAppMessageDto sendLocationMessage(WhatsAppMessageDto.SendLocationRequest request) {
        WhatsAppConfiguration config = configRepository.findByChannelId(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));

        if (!config.getEnabled()) {
            throw new ServiceDeskException("WhatsApp channel is disabled");
        }

        WhatsAppMessage message = WhatsAppMessage.builder()
                .channelId(request.getChannelId())
                .fromNumber(config.getDisplayPhoneNumber())
                .toNumber(request.getToNumber())
                .ticketId(request.getTicketId())
                .direction(WhatsAppMessage.Direction.OUTBOUND)
                .messageType(WhatsAppMessage.MessageType.LOCATION)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationName(request.getName())
                .locationAddress(request.getAddress())
                .status(WhatsAppMessage.Status.PENDING)
                .build();

        message = messageRepository.save(message);

        sendLocationMessageAsync(message, config);

        return toMessageDto(message);
    }

    @Async
    public void sendLocationMessageAsync(WhatsAppMessage message, WhatsAppConfiguration config) {
        try {
            String url = buildApiUrl(config, "messages");

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", message.getToNumber());
            body.put("type", "location");

            Map<String, Object> location = new HashMap<>();
            location.put("latitude", message.getLatitude());
            location.put("longitude", message.getLongitude());
            if (message.getLocationName() != null) {
                location.put("name", message.getLocationName());
            }
            if (message.getLocationAddress() != null) {
                location.put("address", message.getLocationAddress());
            }
            body.put("location", location);

            JsonNode response = sendApiRequest(url, config.getAccessToken(), body);

            if (response.has("messages") && response.get("messages").isArray()) {
                String messageId = response.get("messages").get(0).get("id").asText();
                message.setWhatsappMessageId(messageId);
                message.setStatus(WhatsAppMessage.Status.SENT);
                message.setSentAt(LocalDateTime.now());
            }

            messageRepository.save(message);
            log.info("WhatsApp location message sent: {} to {}", message.getId(), message.getToNumber());

        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    // ==================== Media Download ====================

    public String getMediaUrl(String channelId, String mediaId) {
        WhatsAppConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsApp configuration not found"));

        try {
            String url = WHATSAPP_API_URL + config.getApiVersion().getVersion() + "/" + mediaId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.getAccessToken());

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                return responseBody.get("url").asText();
            }

            throw new ServiceDeskException("Failed to get media URL");

        } catch (Exception e) {
            log.error("Failed to get media URL: {}", e.getMessage());
            throw new ServiceDeskException("Failed to get media URL: " + e.getMessage());
        }
    }

    // ==================== Message Queries ====================

    public List<WhatsAppMessageDto> getMessagesByTicket(String ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    public List<WhatsAppMessageDto> getConversationHistory(String channelId, String phoneNumber) {
        return messageRepository.findConversationHistory(channelId, phoneNumber)
                .stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    private String buildApiUrl(WhatsAppConfiguration config, String endpoint) {
        return WHATSAPP_API_URL + config.getApiVersion().getVersion() + "/" +
                config.getPhoneNumberId() + "/" + endpoint;
    }

    private JsonNode sendApiRequest(String url, String accessToken, Map<String, Object> body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceDeskException("WhatsApp API error: " + response.getBody());
        }

        return objectMapper.readTree(response.getBody());
    }

    private void handleSendError(WhatsAppMessage message, Exception e) {
        log.error("Failed to send WhatsApp message: {}", message.getId(), e);
        message.setStatus(WhatsAppMessage.Status.FAILED);
        message.setFailedAt(LocalDateTime.now());
        message.setErrorMessage(e.getMessage());
        message.setRetryCount(message.getRetryCount() + 1);
        messageRepository.save(message);
    }

    private List<Map<String, Object>> buildParameters(Object[] params) {
        List<Map<String, Object>> parameters = new java.util.ArrayList<>();
        for (Object param : params) {
            Map<String, Object> p = new HashMap<>();
            p.put("type", "text");
            p.put("text", param.toString());
            parameters.add(p);
        }
        return parameters;
    }

    private WhatsAppMessage.MessageType determineMessageType(String type) {
        if (type == null) return WhatsAppMessage.MessageType.UNKNOWN;

        return switch (type.toLowerCase()) {
            case "text" -> WhatsAppMessage.MessageType.TEXT;
            case "image" -> WhatsAppMessage.MessageType.IMAGE;
            case "audio" -> WhatsAppMessage.MessageType.AUDIO;
            case "video" -> WhatsAppMessage.MessageType.VIDEO;
            case "document" -> WhatsAppMessage.MessageType.DOCUMENT;
            case "sticker" -> WhatsAppMessage.MessageType.STICKER;
            case "location" -> WhatsAppMessage.MessageType.LOCATION;
            case "contacts" -> WhatsAppMessage.MessageType.CONTACTS;
            case "interactive" -> WhatsAppMessage.MessageType.INTERACTIVE;
            case "button" -> WhatsAppMessage.MessageType.BUTTON;
            case "reaction" -> WhatsAppMessage.MessageType.REACTION;
            default -> WhatsAppMessage.MessageType.UNKNOWN;
        };
    }

    private WhatsAppConfigurationDto toConfigDto(WhatsAppConfiguration config) {
        return WhatsAppConfigurationDto.builder()
                .id(config.getId())
                .channelId(config.getChannelId())
                .name(config.getName())
                .phoneNumberId(config.getPhoneNumberId())
                .businessAccountId(config.getBusinessAccountId())
                .displayPhoneNumber(config.getDisplayPhoneNumber())
                .webhookUrl(config.getWebhookUrl())
                .welcomeMessage(config.getWelcomeMessage())
                .autoCreateTicket(config.getAutoCreateTicket())
                .defaultPriority(config.getDefaultPriority())
                .defaultTeamId(config.getDefaultTeamId())
                .defaultCategoryId(config.getDefaultCategoryId())
                .enabled(config.getEnabled())
                .apiVersion(config.getApiVersion())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private WhatsAppMessageDto toMessageDto(WhatsAppMessage message) {
        return WhatsAppMessageDto.builder()
                .id(message.getId())
                .whatsappMessageId(message.getWhatsappMessageId())
                .channelId(message.getChannelId())
                .ticketId(message.getTicketId())
                .conversationId(message.getConversationId())
                .fromNumber(message.getFromNumber())
                .toNumber(message.getToNumber())
                .contactName(message.getContactName())
                .profileName(message.getProfileName())
                .direction(message.getDirection())
                .messageType(message.getMessageType())
                .text(message.getText())
                .caption(message.getCaption())
                .mediaUrl(message.getMediaUrl())
                .mediaFilename(message.getMediaFilename())
                .mediaMimeType(message.getMediaMimeType())
                .latitude(message.getLatitude())
                .longitude(message.getLongitude())
                .locationName(message.getLocationName())
                .locationAddress(message.getLocationAddress())
                .status(message.getStatus())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .errorMessage(message.getErrorMessage())
                .timestamp(message.getCreatedAt())
                .build();
    }
}
