package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "automation_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AutomationRule rule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(name = "target_field")
    private String targetField;

    @Column(name = "target_value", columnDefinition = "TEXT")
    private String targetValue;

    @Column(name = "execution_order")
    private Integer executionOrder = 0;

    public enum ActionType {
        // Field updates
        SET_STATUS,
        SET_PRIORITY,
        SET_TYPE,
        SET_CATEGORY,
        SET_ASSIGNEE,
        SET_TEAM,
        ADD_TAG,
        REMOVE_TAG,
        SET_DUE_DATE,

        // Notifications
        SEND_EMAIL,
        SEND_NOTIFICATION,
        SEND_WEBHOOK,
        SEND_SLACK,

        // Comments
        ADD_INTERNAL_NOTE,
        ADD_REPLY,

        // Escalation
        ESCALATE,
        REASSIGN,

        // Other
        RUN_SCRIPT,
        CALL_API
    }
}
