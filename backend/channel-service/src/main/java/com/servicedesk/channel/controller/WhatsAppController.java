package com.servicedesk.channel.controller;

import com.servicedesk.channel.dto.WhatsAppConfigurationDto;
import com.servicedesk.channel.dto.WhatsAppMessageDto;
import com.servicedesk.channel.dto.WhatsAppWebhookDto;
import com.servicedesk.channel.service.WhatsAppService;
import com.servicedesk.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/channels/whatsapp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WhatsApp Channel", description = "WhatsApp Business API integration endpoints")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    // ==================== Webhook Endpoints ====================

    @GetMapping("/webhook/{phoneNumberId}")
    @Operation(summary = "Verify WhatsApp webhook", description = "Webhook verification endpoint for Meta")
    public ResponseEntity<String> verifyWebhook(
            @PathVariable String phoneNumberId,
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        log.info("Webhook verification request for phone: {}", phoneNumberId);

        if (whatsAppService.verifyWebhook(phoneNumberId, mode, token, challenge)) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.status(403).body("Verification failed");
    }

    @PostMapping("/webhook/{phoneNumberId}")
    @Operation(summary = "Receive WhatsApp webhook events", description = "Receives incoming messages and status updates")
    public ResponseEntity<Void> receiveWebhook(
            @PathVariable String phoneNumberId,
            @RequestBody WhatsAppWebhookDto webhook) {

        log.debug("Webhook received for phone: {}", phoneNumberId);
        whatsAppService.processWebhook(webhook);
        return ResponseEntity.ok().build();
    }

    // ==================== Configuration Endpoints ====================

    @PostMapping("/configurations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create WhatsApp configuration", description = "Create a new WhatsApp Business API configuration")
    public ResponseEntity<ApiResponse<WhatsAppConfigurationDto>> createConfiguration(
            @Valid @RequestBody WhatsAppConfigurationDto.CreateRequest request) {

        WhatsAppConfigurationDto config = whatsAppService.createConfiguration(request);
        return ResponseEntity.ok(ApiResponse.success(config, "WhatsApp configuration created successfully"));
    }

    @PutMapping("/configurations/{configId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update WhatsApp configuration")
    public ResponseEntity<ApiResponse<WhatsAppConfigurationDto>> updateConfiguration(
            @PathVariable String configId,
            @Valid @RequestBody WhatsAppConfigurationDto.UpdateRequest request) {

        WhatsAppConfigurationDto config = whatsAppService.updateConfiguration(configId, request);
        return ResponseEntity.ok(ApiResponse.success(config, "WhatsApp configuration updated successfully"));
    }

    @GetMapping("/configurations/{configId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get WhatsApp configuration by ID")
    public ResponseEntity<ApiResponse<WhatsAppConfigurationDto>> getConfiguration(@PathVariable String configId) {
        WhatsAppConfigurationDto config = whatsAppService.getConfiguration(configId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @GetMapping("/configurations/channel/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get WhatsApp configuration by channel ID")
    public ResponseEntity<ApiResponse<WhatsAppConfigurationDto>> getConfigurationByChannel(@PathVariable String channelId) {
        WhatsAppConfigurationDto config = whatsAppService.getConfigurationByChannel(channelId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    // ==================== Send Message Endpoints ====================

    @PostMapping("/messages/text")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Send text message", description = "Send a text message to a WhatsApp user")
    public ResponseEntity<ApiResponse<WhatsAppMessageDto>> sendTextMessage(
            @Valid @RequestBody WhatsAppMessageDto.SendTextRequest request) {

        WhatsAppMessageDto message = whatsAppService.sendTextMessage(request);
        return ResponseEntity.ok(ApiResponse.success(message, "Message sent successfully"));
    }

    @PostMapping("/messages/media")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Send media message", description = "Send an image, video, audio, or document")
    public ResponseEntity<ApiResponse<WhatsAppMessageDto>> sendMediaMessage(
            @Valid @RequestBody WhatsAppMessageDto.SendMediaRequest request) {

        WhatsAppMessageDto message = whatsAppService.sendMediaMessage(request);
        return ResponseEntity.ok(ApiResponse.success(message, "Media message sent successfully"));
    }

    @PostMapping("/messages/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Send template message", description = "Send a pre-approved template message")
    public ResponseEntity<ApiResponse<WhatsAppMessageDto>> sendTemplateMessage(
            @Valid @RequestBody WhatsAppMessageDto.SendTemplateRequest request) {

        WhatsAppMessageDto message = whatsAppService.sendTemplateMessage(request);
        return ResponseEntity.ok(ApiResponse.success(message, "Template message sent successfully"));
    }

    @PostMapping("/messages/location")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Send location message", description = "Send a location to a WhatsApp user")
    public ResponseEntity<ApiResponse<WhatsAppMessageDto>> sendLocationMessage(
            @Valid @RequestBody WhatsAppMessageDto.SendLocationRequest request) {

        WhatsAppMessageDto message = whatsAppService.sendLocationMessage(request);
        return ResponseEntity.ok(ApiResponse.success(message, "Location message sent successfully"));
    }

    // ==================== Message Query Endpoints ====================

    @GetMapping("/messages/ticket/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get messages by ticket", description = "Get all WhatsApp messages for a specific ticket")
    public ResponseEntity<ApiResponse<List<WhatsAppMessageDto>>> getMessagesByTicket(@PathVariable String ticketId) {
        List<WhatsAppMessageDto> messages = whatsAppService.getMessagesByTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/messages/conversation")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get conversation history", description = "Get message history with a specific phone number")
    public ResponseEntity<ApiResponse<List<WhatsAppMessageDto>>> getConversationHistory(
            @RequestParam String channelId,
            @RequestParam String phoneNumber) {

        List<WhatsAppMessageDto> messages = whatsAppService.getConversationHistory(channelId, phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    // ==================== Media Endpoints ====================

    @GetMapping("/media/{channelId}/{mediaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get media URL", description = "Get download URL for a media file")
    public ResponseEntity<ApiResponse<String>> getMediaUrl(
            @PathVariable String channelId,
            @PathVariable String mediaId) {

        String url = whatsAppService.getMediaUrl(channelId, mediaId);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
