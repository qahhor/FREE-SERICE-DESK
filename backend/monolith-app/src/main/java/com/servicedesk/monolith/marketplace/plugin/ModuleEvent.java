package com.servicedesk.monolith.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a system event that modules can listen to
 */
@Data
@Builder
public class ModuleEvent {

    /**
     * Event type
     */
    private String type;

    /**
     * Tenant ID
     */
    private UUID tenantId;

    /**
     * User who triggered the event
     */
    private UUID userId;

    /**
     * Event timestamp
     */
    private Instant timestamp;

    /**
     * Event payload
     */
    private Map<String, Object> payload;

    /**
     * Correlation ID for tracing
     */
    private String correlationId;
}
