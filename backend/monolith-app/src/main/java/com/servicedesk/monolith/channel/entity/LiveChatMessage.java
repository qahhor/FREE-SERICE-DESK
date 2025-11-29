package com.servicedesk.monolith.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "livechat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LiveChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_avatar")
    private String senderAvatar;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "attachment_url", length = 1000)
    private String attachmentUrl;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "attachment_size")
    private Long attachmentSize;

    @Column(name = "attachment_mime_type")
    private String attachmentMimeType;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    public enum SenderType {
        VISITOR,
        AGENT,
        SYSTEM,
        BOT
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        AUDIO,
        VIDEO,
        CARD,
        BUTTONS,
        CAROUSEL,
        QUICK_REPLIES,
        FORM,
        SYSTEM
    }
}
