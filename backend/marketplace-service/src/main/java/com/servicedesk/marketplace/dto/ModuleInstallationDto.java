package com.servicedesk.marketplace.dto;

import com.servicedesk.marketplace.entity.ModuleInstallation;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for module installation
 */
@Data
@Builder
public class ModuleInstallationDto {

    private UUID id;
    private UUID tenantId;
    private String moduleId;
    private String moduleName;
    private String moduleIcon;
    private String installedVersion;
    private String latestVersion;
    private boolean updateAvailable;
    private ModuleInstallation.InstallationStatus status;
    private Map<String, Object> configuration;
    private boolean enabled;
    private boolean autoUpdate;
    private String licenseKey;
    private Instant licenseExpiresAt;
    private Instant trialExpiresAt;
    private boolean inTrial;
    private ModuleInstallation.HealthStatus healthStatus;
    private String errorMessage;
    private Map<String, Object> usageStats;
    private Instant createdAt;
    private Instant updatedAt;

    public static ModuleInstallationDto fromEntity(ModuleInstallation entity) {
        return ModuleInstallationDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .moduleId(entity.getModuleId())
                .installedVersion(entity.getInstalledVersion())
                .status(entity.getStatus())
                .configuration(entity.getConfiguration())
                .enabled(entity.isEnabled())
                .autoUpdate(entity.isAutoUpdate())
                .licenseKey(entity.getLicenseKey() != null ? maskLicenseKey(entity.getLicenseKey()) : null)
                .licenseExpiresAt(entity.getLicenseExpiresAt())
                .trialExpiresAt(entity.getTrialExpiresAt())
                .inTrial(entity.getTrialExpiresAt() != null && entity.getTrialExpiresAt().isAfter(Instant.now()))
                .healthStatus(entity.getHealthStatus())
                .errorMessage(entity.getErrorMessage())
                .usageStats(entity.getUsageStats())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static String maskLicenseKey(String key) {
        if (key == null || key.length() < 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}
