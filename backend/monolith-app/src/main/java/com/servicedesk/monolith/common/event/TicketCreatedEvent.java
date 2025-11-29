package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Event fired when a ticket is created (replaces RabbitMQ ticket.queue)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreatedEvent {
    private UUID ticketId;
    private String subject;
    private String description;
    private UUID projectId;
    private UUID categoryId;
    private String priority;
    private String type;
    private UUID customerId;
    private String channel; // EMAIL, TELEGRAM, WHATSAPP, WIDGET, LIVECHAT
    private String externalId; // External message/conversation ID
    private Map<String, Object> metadata;
}
