package com.servicedesk.monolith.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "livechat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveChatSession extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "visitor_id", nullable = false)
    private String visitorId;

    @Column(name = "visitor_name")
    private String visitorName;

    @Column(name = "visitor_email")
    private String visitorEmail;

    @Column(name = "visitor_phone")
    private String visitorPhone;

    @Column(name = "assigned_agent_id")
    private String assignedAgentId;

    @Column(name = "assigned_agent_name")
    private String assignedAgentName;

    @Column(name = "ticket_id")
    private String ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.WAITING;

    @Column
    private String department;

    @Column(name = "page_url", length = 1000)
    private String pageUrl;

    @Column(name = "page_title", length = 500)
    private String pageTitle;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column
    @Builder.Default
    private String locale = "en";

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "visitor_info", columnDefinition = "jsonb")
    private String visitorInfo;

    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private String customFields;

    @Column(name = "tags")
    private String tags;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LiveChatMessage> messages = new ArrayList<>();

    public enum SessionStatus {
        WAITING,
        ACTIVE,
        ON_HOLD,
        TRANSFERRED,
        CLOSED,
        ABANDONED,
        MISSED
    }

    public void addMessage(LiveChatMessage message) {
        messages.add(message);
        message.setSession(this);
        this.messageCount = this.messageCount + 1;
        this.lastActivityAt = LocalDateTime.now();
    }
}
