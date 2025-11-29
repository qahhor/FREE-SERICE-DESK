package com.servicedesk.monolith.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for installing a module
 */
@Data
public class InstallModuleRequest {

    @NotBlank(message = "Module ID is required")
    private String moduleId;

    private String version;

    private Map<String, Object> configuration;

    private String licenseKey;

    private boolean enableTrial;

    private boolean autoUpdate;
}
