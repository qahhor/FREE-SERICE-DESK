package com.servicedesk.monolith.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an installed module for a tenant
 */
@Entity
@Table(name = "module_installations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "module_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "module_id", nullable = false)
    private String moduleId;

    @Column(name = "installed_version", nullable = false)
    private String installedVersion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InstallationStatus status = InstallationStatus.INSTALLING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> configuration = new HashMap<>();

    @Column(name = "installed_by")
    private UUID installedBy;

    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "auto_update")
    @Builder.Default
    private boolean autoUpdate = false;

    @Column(name = "license_key")
    private String licenseKey;

    @Column(name = "license_expires_at")
    private Instant licenseExpiresAt;

    @Column(name = "trial_started_at")
    private Instant trialStartedAt;

    @Column(name = "trial_expires_at")
    private Instant trialExpiresAt;

    @Column(name = "last_health_check")
    private Instant lastHealthCheck;

    @Column(name = "health_status")
    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "usage_stats", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> usageStats = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum InstallationStatus {
        INSTALLING,
        ACTIVE,
        DISABLED,
        UPDATING,
        FAILED,
        UNINSTALLING,
        UNINSTALLED
    }

    public enum HealthStatus {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }
}
