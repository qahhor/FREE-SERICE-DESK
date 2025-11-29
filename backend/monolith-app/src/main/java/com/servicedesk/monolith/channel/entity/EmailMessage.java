package com.servicedesk.monolith.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage extends BaseEntity {

    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "ticket_id")
    private String ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "to_addresses", columnDefinition = "TEXT")
    private String toAddresses;

    @Column(name = "cc_addresses", columnDefinition = "TEXT")
    private String ccAddresses;

    @Column(name = "bcc_addresses", columnDefinition = "TEXT")
    private String bccAddresses;

    @Column(nullable = false)
    private String subject;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "in_reply_to")
    private String inReplyTo;

    @Column(name = "references_header", columnDefinition = "TEXT")
    private String referencesHeader;

    @Column(name = "has_attachments")
    private Boolean hasAttachments = false;

    @Column(name = "attachment_count")
    private Integer attachmentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    public enum Status {
        PENDING,
        PROCESSING,
        SENT,
        DELIVERED,
        FAILED,
        BOUNCED
    }
}
