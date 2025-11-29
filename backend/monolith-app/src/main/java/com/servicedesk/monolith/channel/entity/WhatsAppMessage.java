package com.servicedesk.monolith.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppMessage extends BaseEntity {

    @Column(name = "whatsapp_message_id", unique = true)
    private String whatsappMessageId;

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "ticket_id")
    private String ticketId;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "from_number", nullable = false)
    private String fromNumber;

    @Column(name = "to_number", nullable = false)
    private String toNumber;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "profile_name")
    private String profileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "media_id")
    private String mediaId;

    @Column(name = "media_url", length = 1000)
    private String mediaUrl;

    @Column(name = "media_mime_type")
    private String mediaMimeType;

    @Column(name = "media_sha256")
    private String mediaSha256;

    @Column(name = "media_filename")
    private String mediaFilename;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "contact_vcard", columnDefinition = "TEXT")
    private String contactVcard;

    @Column(name = "button_text")
    private String buttonText;

    @Column(name = "button_payload")
    private String buttonPayload;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "template_language")
    private String templateLanguage;

    @Column(name = "context_message_id")
    private String contextMessageId;

    @Column(name = "context_from")
    private String contextFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        VIDEO,
        DOCUMENT,
        STICKER,
        LOCATION,
        CONTACTS,
        INTERACTIVE,
        BUTTON,
        TEMPLATE,
        REACTION,
        UNKNOWN
    }

    public enum Status {
        PENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }
}
