package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "automation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_id")
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerEvent triggerEvent;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "execution_order")
    private Integer executionOrder = 0;

    @Column(name = "stop_processing")
    private Boolean stopProcessing = false;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutomationCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutomationAction> actions = new ArrayList<>();

    @Column(name = "execution_count")
    private Long executionCount = 0L;

    public enum TriggerEvent {
        TICKET_CREATED,
        TICKET_UPDATED,
        TICKET_ASSIGNED,
        TICKET_STATUS_CHANGED,
        TICKET_PRIORITY_CHANGED,
        COMMENT_ADDED,
        SLA_WARNING,
        SLA_BREACHED,
        SCHEDULED
    }
}
