package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Event for triggering automation rules (replaces RabbitMQ automation.events)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationEvent {
    private String triggerType; // TICKET_CREATED, TICKET_UPDATED, TICKET_CLOSED, etc.
    private UUID entityId;
    private String entityType; // TICKET, CHANGE, PROBLEM
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private UUID triggeredByUserId;
}
