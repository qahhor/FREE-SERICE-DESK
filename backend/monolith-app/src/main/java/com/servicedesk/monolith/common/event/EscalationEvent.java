package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event for ticket escalation (replaces RabbitMQ escalation.events)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationEvent {
    private UUID ticketId;
    private UUID escalationRuleId;
    private String escalationType; // SLA_BREACH, PRIORITY_INCREASE, REASSIGNMENT
    private UUID fromAssigneeId;
    private UUID toAssigneeId;
    private UUID fromTeamId;
    private UUID toTeamId;
    private String reason;
    private Integer escalationLevel;
}
