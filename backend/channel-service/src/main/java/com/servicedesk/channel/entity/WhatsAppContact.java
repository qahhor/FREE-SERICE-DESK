package com.servicedesk.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppContact extends BaseEntity {

    @Column(name = "channel_id", nullable = false)
    private String channelId;

    @Column(name = "wa_id", nullable = false)
    private String waId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "ticket_id")
    private String ticketId;

    @Column(name = "first_message_at")
    private LocalDateTime firstMessageAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "blocked_reason")
    private String blockedReason;

    @Column(columnDefinition = "jsonb")
    private String metadata;
}
