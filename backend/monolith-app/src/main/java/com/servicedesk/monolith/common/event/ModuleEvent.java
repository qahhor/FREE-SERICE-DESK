package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event for module lifecycle (replaces RabbitMQ module-events)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleEvent {
    private UUID moduleId;
    private String moduleKey;
    private String eventType; // INSTALLED, UNINSTALLED, ENABLED, DISABLED, UPDATED
    private String version;
    private UUID tenantId;
}
