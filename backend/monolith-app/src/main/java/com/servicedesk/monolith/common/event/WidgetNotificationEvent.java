package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event for widget real-time notifications (replaces RabbitMQ widget.notifications)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetNotificationEvent {
    private UUID conversationId;
    private UUID messageId;
    private String messageType; // CUSTOMER_MESSAGE, AGENT_MESSAGE, SYSTEM_MESSAGE
    private UUID agentId;
    private String content;
    private String status; // NEW, READ, REPLIED
}
