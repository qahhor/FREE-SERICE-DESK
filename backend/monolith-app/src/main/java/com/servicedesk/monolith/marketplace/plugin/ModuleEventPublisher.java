package com.servicedesk.monolith.marketplace.plugin;

import java.util.Map;
import java.util.UUID;

/**
 * Event publisher interface for modules to emit events
 */
public interface ModuleEventPublisher {

    /**
     * Publish an event synchronously
     */
    void publish(String eventType, Map<String, Object> payload);

    /**
     * Publish an event asynchronously
     */
    void publishAsync(String eventType, Map<String, Object> payload);

    /**
     * Publish an event with specific tenant context
     */
    void publish(String eventType, UUID tenantId, Map<String, Object> payload);

    /**
     * Publish a module-specific event
     * Event type will be prefixed with module ID
     */
    void publishModuleEvent(String moduleId, String eventType, Map<String, Object> payload);
}
