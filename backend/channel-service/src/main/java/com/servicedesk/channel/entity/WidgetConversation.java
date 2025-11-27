package com.servicedesk.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "widget_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetConversation extends BaseEntity {

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

    @Column(name = "ticket_id")
    private String ticketId;

    @Column(name = "assigned_agent_id")
    private String assignedAgentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "page_url")
    private String pageUrl;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column
    private String locale = "en";

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<WidgetMessage> messages = new ArrayList<>();

    public enum ConversationStatus {
        ACTIVE,
        WAITING,
        ASSIGNED,
        CLOSED
    }
}
