package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sla_breach_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBreachHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id")
    private SlaPolicy slaPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_type", nullable = false)
    private BreachType breachType;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "breached_at", nullable = false)
    private LocalDateTime breachedAt;

    @Column(name = "breach_duration_minutes", nullable = false)
    private Integer breachDurationMinutes;

    @Column(name = "escalation_triggered")
    @Builder.Default
    private Boolean escalationTriggered = false;

    public enum BreachType {
        FIRST_RESPONSE,
        RESOLUTION,
        NEXT_RESPONSE,
        UPDATE
    }
}
