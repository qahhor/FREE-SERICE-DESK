package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event for sending webhooks (replaces RabbitMQ webhook.queue)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {
    private String url;
    private String method; // GET, POST, PUT, PATCH, DELETE
    private Map<String, String> headers;
    private Map<String, Object> payload;
    private String eventType;
    private Integer retryCount;
}
