package com.servicedesk.monolith.notification.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.NORMAL;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    public enum NotificationType {
        TICKET_ASSIGNED,
        TICKET_UPDATED,
        TICKET_COMMENTED,
        TICKET_RESOLVED,
        TICKET_ESCALATED,
        TICKET_MENTIONED,
        SLA_WARNING,
        SLA_BREACHED,
        TEAM_MENTION,
        SYSTEM_ANNOUNCEMENT,
        REPORT_READY,
        CUSTOM
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
