package com.servicedesk.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramMessage extends BaseEntity {

    @Column(name = "telegram_message_id")
    private Long telegramMessageId;

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "ticket_id")
    private String ticketId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "chat_type")
    private String chatType;

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "from_username")
    private String fromUsername;

    @Column(name = "from_first_name")
    private String fromFirstName;

    @Column(name = "from_last_name")
    private String fromLastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    public enum MessageType {
        TEXT,
        PHOTO,
        DOCUMENT,
        VIDEO,
        AUDIO,
        VOICE,
        STICKER,
        CONTACT,
        LOCATION
    }

    public enum Status {
        PENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }
}
