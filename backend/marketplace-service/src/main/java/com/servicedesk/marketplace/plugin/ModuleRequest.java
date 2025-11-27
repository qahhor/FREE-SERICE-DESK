package com.servicedesk.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Represents an HTTP request to a module endpoint
 */
@Data
@Builder
public class ModuleRequest {

    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> pathParams;
    private String body;
    private UUID userId;
    private UUID tenantId;
    private Map<String, Object> moduleConfig;
}
