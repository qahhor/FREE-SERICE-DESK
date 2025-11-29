package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaTarget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SlaPolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ticket.TicketPriority priority;

    @Column(name = "target_minutes", nullable = false)
    private Integer targetMinutes;

    @Column(name = "warning_minutes")
    private Integer warningMinutes;

    public enum TargetType {
        FIRST_RESPONSE,   // Time to first response
        RESOLUTION,       // Time to resolution
        NEXT_RESPONSE,    // Time between responses
        UPDATE            // Time between updates
    }
}
