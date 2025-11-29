package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "escalation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id")
    private SlaPolicy slaPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(name = "trigger_minutes")
    private Integer triggerMinutes;

    @Column(name = "escalation_level", nullable = false)
    @Builder.Default
    private Integer escalationLevel = 1;

    @Column(name = "notify_assignee")
    @Builder.Default
    private Boolean notifyAssignee = true;

    @Column(name = "notify_team_lead")
    @Builder.Default
    private Boolean notifyTeamLead = false;

    @Column(name = "notify_manager")
    @Builder.Default
    private Boolean notifyManager = false;

    @Column(name = "notify_custom_users", columnDefinition = "TEXT")
    private String notifyCustomUsers;

    @Column(name = "notify_email", columnDefinition = "TEXT")
    private String notifyEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reassign_to_user_id")
    private User reassignToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reassign_to_team_id")
    private Team reassignToTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_priority")
    private Ticket.TicketPriority changePriority;

    @Column(name = "add_tags", columnDefinition = "TEXT")
    private String addTags;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "execution_order")
    @Builder.Default
    private Integer executionOrder = 0;

    public enum TriggerType {
        SLA_WARNING,      // Before SLA breach
        SLA_BREACH,       // When SLA is breached
        TIME_BASED,       // After certain time without update
        NO_RESPONSE,      // Customer hasn't responded
        REOPENED          // Ticket reopened
    }
}
