package com.servicedesk.monolith.channel.dto;

import com.servicedesk.monolith.channel.entity.WhatsAppMessage;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppMessageDto {

    private String id;
    private String whatsappMessageId;
    private String channelId;
    private String ticketId;
    private String conversationId;
    private String fromNumber;
    private String toNumber;
    private String contactName;
    private String profileName;
    private WhatsAppMessage.Direction direction;
    private WhatsAppMessage.MessageType messageType;
    private String text;
    private String caption;
    private String mediaUrl;
    private String mediaFilename;
    private String mediaMimeType;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String locationAddress;
    private WhatsAppMessage.Status status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private String errorMessage;
    private LocalDateTime timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendTextRequest {
        private String channelId;
        private String toNumber;
        private String text;
        private String ticketId;
        private String contextMessageId;
        private Boolean previewUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMediaRequest {
        private String channelId;
        private String toNumber;
        private WhatsAppMessage.MessageType messageType;
        private String mediaUrl;
        private String caption;
        private String filename;
        private String ticketId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendTemplateRequest {
        private String channelId;
        private String toNumber;
        private String templateName;
        private String templateLanguage;
        private Object[] headerParameters;
        private Object[] bodyParameters;
        private Object[] buttonParameters;
        private String ticketId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendLocationRequest {
        private String channelId;
        private String toNumber;
        private Double latitude;
        private Double longitude;
        private String name;
        private String address;
        private String ticketId;
    }
}
