package com.servicedesk.monolith.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a REST endpoint provided by a module
 */
@Data
@Builder
public class ModuleEndpoint {

    /**
     * HTTP method (GET, POST, PUT, DELETE, PATCH)
     */
    private String method;

    /**
     * Path relative to module base URL
     */
    private String path;

    /**
     * Description of the endpoint
     */
    private String description;

    /**
     * Required permissions to access this endpoint
     */
    private List<String> requiredPermissions;

    /**
     * Request handler function
     */
    private Function<ModuleRequest, ModuleResponse> handler;

    /**
     * Rate limit (requests per minute), 0 for no limit
     */
    @Builder.Default
    private int rateLimit = 0;

    /**
     * Whether this endpoint is public (no auth required)
     */
    @Builder.Default
    private boolean publicEndpoint = false;
}
