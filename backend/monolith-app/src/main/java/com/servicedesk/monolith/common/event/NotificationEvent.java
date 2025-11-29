package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Event for sending notifications (replaces RabbitMQ notification.queue)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID userId;
    private String type; // EMAIL, IN_APP, PUSH, SMS
    private String title;
    private String message;
    private String category; // TICKET_ASSIGNED, SLA_BREACH, ESCALATION, etc.
    private UUID relatedEntityId;
    private String relatedEntityType; // TICKET, CHANGE, PROBLEM, etc.
    private Map<String, Object> data;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
}
