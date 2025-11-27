package com.servicedesk.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Context provided to modules during lifecycle events
 */
@Data
@Builder
public class ModuleContext {

    /**
     * Tenant ID for multi-tenant installations
     */
    private UUID tenantId;

    /**
     * Current module configuration
     */
    private Map<String, Object> configuration;

    /**
     * Installation ID
     */
    private UUID installationId;

    /**
     * Data storage path for module files
     */
    private String dataPath;

    /**
     * Base URL for the module's API endpoints
     */
    private String baseUrl;

    /**
     * Service locator for accessing platform services
     */
    private ModuleServiceLocator serviceLocator;
}
