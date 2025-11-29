package com.servicedesk.monolith.channel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.servicedesk.monolith.channel.entity.TelegramConfiguration;
import com.servicedesk.monolith.channel.entity.TelegramMessage;
import com.servicedesk.monolith.channel.repository.TelegramConfigurationRepository;
import com.servicedesk.monolith.channel.service.TelegramService;
import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.exception.ResourceNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/channels/telegram")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramConfigurationRepository configRepository;
    private final TelegramService telegramService;

    @GetMapping("/configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TelegramConfigurationDto>>> getAllConfigurations() {
        List<TelegramConfigurationDto> configs = configRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/configurations/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TelegramConfigurationDto>> getConfiguration(@PathVariable String channelId) {
        TelegramConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Telegram configuration not found"));
        return ResponseEntity.ok(ApiResponse.success(toDto(config)));
    }

    @PostMapping("/configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TelegramConfigurationDto>> createConfiguration(
            @RequestBody TelegramConfigurationDto request) {
        if (configRepository.existsByBotToken(request.getBotToken())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Bot token already configured"));
        }

        TelegramConfiguration config = TelegramConfiguration.builder()
                .channelId(request.getChannelId())
                .name(request.getName())
                .botToken(request.getBotToken())
                .botUsername(request.getBotUsername())
                .webhookUrl(request.getWebhookUrl())
                .webhookSecret(request.getWebhookSecret())
                .useWebhook(request.getUseWebhook() != null ? request.getUseWebhook() : false)
                .welcomeMessage(request.getWelcomeMessage())
                .autoCreateTicket(request.getAutoCreateTicket() != null ? request.getAutoCreateTicket() : true)
                .defaultPriority(request.getDefaultPriority() != null ? request.getDefaultPriority() : "MEDIUM")
                .defaultTeamId(request.getDefaultTeamId())
                .defaultCategoryId(request.getDefaultCategoryId())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        config = configRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success(toDto(config)));
    }

    @PutMapping("/configurations/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TelegramConfigurationDto>> updateConfiguration(
            @PathVariable String channelId,
            @RequestBody TelegramConfigurationDto request) {
        TelegramConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Telegram configuration not found"));

        if (request.getName() != null) config.setName(request.getName());
        if (request.getBotToken() != null) config.setBotToken(request.getBotToken());
        if (request.getBotUsername() != null) config.setBotUsername(request.getBotUsername());
        if (request.getWebhookUrl() != null) config.setWebhookUrl(request.getWebhookUrl());
        if (request.getWebhookSecret() != null) config.setWebhookSecret(request.getWebhookSecret());
        if (request.getUseWebhook() != null) config.setUseWebhook(request.getUseWebhook());
        if (request.getWelcomeMessage() != null) config.setWelcomeMessage(request.getWelcomeMessage());
        if (request.getAutoCreateTicket() != null) config.setAutoCreateTicket(request.getAutoCreateTicket());
        if (request.getDefaultPriority() != null) config.setDefaultPriority(request.getDefaultPriority());
        if (request.getDefaultTeamId() != null) config.setDefaultTeamId(request.getDefaultTeamId());
        if (request.getDefaultCategoryId() != null) config.setDefaultCategoryId(request.getDefaultCategoryId());
        if (request.getEnabled() != null) config.setEnabled(request.getEnabled());

        config = configRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success(toDto(config)));
    }

    @PostMapping("/webhook/{channelId}")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable String channelId,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody JsonNode update) {

        TelegramConfiguration config = configRepository.findByChannelId(channelId).orElse(null);

        if (config == null || !config.getEnabled()) {
            return ResponseEntity.ok().build();
        }

        // Validate secret token if configured
        if (config.getWebhookSecret() != null && !config.getWebhookSecret().equals(secretToken)) {
            return ResponseEntity.status(403).build();
        }

        telegramService.processWebhookUpdate(channelId, update);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<TelegramMessage>> sendMessage(
            @RequestBody SendTelegramMessageRequest request) {
        TelegramMessage message = telegramService.sendMessage(
                request.getChannelId(),
                request.getChatId(),
                request.getText(),
                request.getReplyToMessageId()
        );
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/messages/ticket/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<TelegramMessage>>> getMessagesByTicket(@PathVariable String ticketId) {
        List<TelegramMessage> messages = telegramService.getMessagesByTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/configurations/{channelId}/webhook")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> setWebhook(
            @PathVariable String channelId,
            @RequestBody SetWebhookRequest request) {
        telegramService.setWebhook(channelId, request.getWebhookUrl());
        return ResponseEntity.ok(ApiResponse.success("Webhook set successfully"));
    }

    private TelegramConfigurationDto toDto(TelegramConfiguration config) {
        return TelegramConfigurationDto.builder()
                .id(config.getId())
                .channelId(config.getChannelId())
                .name(config.getName())
                .botUsername(config.getBotUsername())
                .webhookUrl(config.getWebhookUrl())
                .useWebhook(config.getUseWebhook())
                .welcomeMessage(config.getWelcomeMessage())
                .autoCreateTicket(config.getAutoCreateTicket())
                .defaultPriority(config.getDefaultPriority())
                .defaultTeamId(config.getDefaultTeamId())
                .defaultCategoryId(config.getDefaultCategoryId())
                .enabled(config.getEnabled())
                .build();
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TelegramConfigurationDto {
        private String id;
        private String channelId;
        private String name;
        private String botToken;
        private String botUsername;
        private String webhookUrl;
        private String webhookSecret;
        private Boolean useWebhook;
        private String welcomeMessage;
        private Boolean autoCreateTicket;
        private String defaultPriority;
        private String defaultTeamId;
        private String defaultCategoryId;
        private Boolean enabled;
    }

    @Data
    public static class SendTelegramMessageRequest {
        private String channelId;
        private Long chatId;
        private String text;
        private Long replyToMessageId;
    }

    @Data
    public static class SetWebhookRequest {
        private String webhookUrl;
    }
}
