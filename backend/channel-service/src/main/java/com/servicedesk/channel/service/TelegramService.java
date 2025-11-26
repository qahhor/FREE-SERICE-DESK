package com.servicedesk.channel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.channel.entity.TelegramConfiguration;
import com.servicedesk.channel.entity.TelegramMessage;
import com.servicedesk.channel.repository.TelegramConfigurationRepository;
import com.servicedesk.channel.repository.TelegramMessageRepository;
import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.common.exception.ServiceDeskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramConfigurationRepository configRepository;
    private final TelegramMessageRepository messageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String TICKET_QUEUE = "ticket.telegram.inbound";

    @Transactional
    public TelegramMessage sendMessage(String channelId, Long chatId, String text, Long replyToMessageId) {
        TelegramConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Telegram configuration not found"));

        if (!config.getEnabled()) {
            throw new ServiceDeskException("Telegram channel is disabled");
        }

        TelegramMessage message = TelegramMessage.builder()
                .channelId(channelId)
                .chatId(chatId)
                .direction(TelegramMessage.Direction.OUTBOUND)
                .text(text)
                .messageType(TelegramMessage.MessageType.TEXT)
                .replyToMessageId(replyToMessageId)
                .status(TelegramMessage.Status.PENDING)
                .build();

        message = messageRepository.save(message);

        sendMessageAsync(message, config);

        return message;
    }

    @Async
    public void sendMessageAsync(TelegramMessage message, TelegramConfiguration config) {
        try {
            String url = TELEGRAM_API_URL + config.getBotToken() + "/sendMessage";

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", message.getChatId());
            body.put("text", message.getText());
            body.put("parse_mode", "HTML");

            if (message.getReplyToMessageId() != null) {
                body.put("reply_to_message_id", message.getReplyToMessageId());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                if (responseBody.get("ok").asBoolean()) {
                    JsonNode result = responseBody.get("result");
                    message.setTelegramMessageId(result.get("message_id").asLong());
                    message.setStatus(TelegramMessage.Status.SENT);
                    message.setSentAt(LocalDateTime.now());
                } else {
                    throw new ServiceDeskException("Telegram API error: " + responseBody.get("description").asText());
                }
            } else {
                throw new ServiceDeskException("Failed to send message: " + response.getStatusCode());
            }

            messageRepository.save(message);
            log.info("Telegram message sent successfully: {}", message.getId());

        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", message.getId(), e);
            message.setStatus(TelegramMessage.Status.FAILED);
            message.setErrorMessage(e.getMessage());
            messageRepository.save(message);
        }
    }

    public void processWebhookUpdate(String channelId, JsonNode update) {
        TelegramConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Telegram configuration not found"));

        if (!config.getEnabled()) {
            log.warn("Received webhook for disabled channel: {}", channelId);
            return;
        }

        if (update.has("message")) {
            processInboundMessage(update.get("message"), config);
        } else if (update.has("callback_query")) {
            processCallbackQuery(update.get("callback_query"), config);
        }
    }

    private void processInboundMessage(JsonNode messageNode, TelegramConfiguration config) {
        Long messageId = messageNode.get("message_id").asLong();
        Long chatId = messageNode.get("chat").get("id").asLong();

        if (messageRepository.existsByTelegramMessageIdAndChatId(messageId, chatId)) {
            log.debug("Message already processed: {} in chat {}", messageId, chatId);
            return;
        }

        JsonNode fromNode = messageNode.get("from");
        JsonNode chatNode = messageNode.get("chat");

        TelegramMessage.MessageType messageType = TelegramMessage.MessageType.TEXT;
        String text = null;
        String fileId = null;
        String fileName = null;

        if (messageNode.has("text")) {
            text = messageNode.get("text").asText();
        } else if (messageNode.has("photo")) {
            messageType = TelegramMessage.MessageType.PHOTO;
            JsonNode photos = messageNode.get("photo");
            fileId = photos.get(photos.size() - 1).get("file_id").asText();
            text = messageNode.has("caption") ? messageNode.get("caption").asText() : null;
        } else if (messageNode.has("document")) {
            messageType = TelegramMessage.MessageType.DOCUMENT;
            JsonNode doc = messageNode.get("document");
            fileId = doc.get("file_id").asText();
            fileName = doc.has("file_name") ? doc.get("file_name").asText() : null;
            text = messageNode.has("caption") ? messageNode.get("caption").asText() : null;
        }

        TelegramMessage message = TelegramMessage.builder()
                .telegramMessageId(messageId)
                .channelId(config.getChannelId())
                .chatId(chatId)
                .chatType(chatNode.get("type").asText())
                .fromUserId(fromNode.get("id").asLong())
                .fromUsername(fromNode.has("username") ? fromNode.get("username").asText() : null)
                .fromFirstName(fromNode.has("first_name") ? fromNode.get("first_name").asText() : null)
                .fromLastName(fromNode.has("last_name") ? fromNode.get("last_name").asText() : null)
                .direction(TelegramMessage.Direction.INBOUND)
                .text(text)
                .messageType(messageType)
                .fileId(fileId)
                .fileName(fileName)
                .replyToMessageId(messageNode.has("reply_to_message") ?
                        messageNode.get("reply_to_message").get("message_id").asLong() : null)
                .status(TelegramMessage.Status.DELIVERED)
                .build();

        message = messageRepository.save(message);

        // Handle /start command
        if (text != null && text.startsWith("/start")) {
            handleStartCommand(message, config);
            return;
        }

        // Send to ticket processing queue
        Map<String, Object> event = new HashMap<>();
        event.put("telegramMessageId", message.getId());
        event.put("channelId", config.getChannelId());
        event.put("chatId", chatId);
        event.put("fromUserId", message.getFromUserId());
        event.put("fromUsername", message.getFromUsername());
        event.put("text", text);
        event.put("autoCreateTicket", config.getAutoCreateTicket());
        event.put("defaultPriority", config.getDefaultPriority());
        event.put("defaultTeamId", config.getDefaultTeamId());

        rabbitTemplate.convertAndSend(TICKET_QUEUE, event);

        log.info("Telegram message processed: {} from user {}", message.getId(), message.getFromUserId());
    }

    private void handleStartCommand(TelegramMessage inboundMessage, TelegramConfiguration config) {
        String welcomeMessage = config.getWelcomeMessage();
        if (welcomeMessage == null || welcomeMessage.isEmpty()) {
            welcomeMessage = "Welcome to our support! Please describe your issue and we'll get back to you shortly.";
        }

        sendMessage(config.getChannelId(), inboundMessage.getChatId(), welcomeMessage, inboundMessage.getTelegramMessageId());
    }

    private void processCallbackQuery(JsonNode callbackQuery, TelegramConfiguration config) {
        String callbackData = callbackQuery.get("data").asText();
        Long chatId = callbackQuery.get("message").get("chat").get("id").asLong();

        log.info("Received callback query: {} from chat {}", callbackData, chatId);

        // Answer callback query to remove loading state
        answerCallbackQuery(callbackQuery.get("id").asText(), config);
    }

    private void answerCallbackQuery(String callbackQueryId, TelegramConfiguration config) {
        try {
            String url = TELEGRAM_API_URL + config.getBotToken() + "/answerCallbackQuery";

            Map<String, Object> body = new HashMap<>();
            body.put("callback_query_id", callbackQueryId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            log.error("Failed to answer callback query: {}", e.getMessage());
        }
    }

    public void fetchUpdates(TelegramConfiguration config) {
        if (!config.getEnabled() || config.getUseWebhook()) {
            return;
        }

        try {
            String url = TELEGRAM_API_URL + config.getBotToken() + "/getUpdates";

            Map<String, Object> body = new HashMap<>();
            if (config.getLastUpdateId() != null) {
                body.put("offset", config.getLastUpdateId() + 1);
            }
            body.put("timeout", 30);
            body.put("allowed_updates", List.of("message", "callback_query"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                if (responseBody.get("ok").asBoolean()) {
                    JsonNode results = responseBody.get("result");
                    for (JsonNode update : results) {
                        processWebhookUpdate(config.getChannelId(), update);
                        config.setLastUpdateId(update.get("update_id").asLong());
                    }
                    configRepository.save(config);
                }
            }

        } catch (Exception e) {
            log.error("Failed to fetch Telegram updates for {}: {}", config.getBotUsername(), e.getMessage());
        }
    }

    public void setWebhook(String channelId, String webhookUrl) {
        TelegramConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Telegram configuration not found"));

        try {
            String url = TELEGRAM_API_URL + config.getBotToken() + "/setWebhook";

            Map<String, Object> body = new HashMap<>();
            body.put("url", webhookUrl);
            if (config.getWebhookSecret() != null) {
                body.put("secret_token", config.getWebhookSecret());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                if (responseBody.get("ok").asBoolean()) {
                    config.setWebhookUrl(webhookUrl);
                    config.setUseWebhook(true);
                    configRepository.save(config);
                    log.info("Webhook set successfully for bot: {}", config.getBotUsername());
                } else {
                    throw new ServiceDeskException("Failed to set webhook: " + responseBody.get("description").asText());
                }
            }

        } catch (Exception e) {
            log.error("Failed to set webhook: {}", e.getMessage());
            throw new ServiceDeskException("Failed to set webhook: " + e.getMessage());
        }
    }

    public List<TelegramMessage> getMessagesByTicket(String ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }
}
