package com.servicedesk.monolith.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a specific version of a module
 */
@Entity
@Table(name = "module_versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @ToString.Exclude
    private MarketplaceModule module;

    @Column(nullable = false)
    private String version;

    @Column(name = "release_notes", columnDefinition = "TEXT")
    private String releaseNotes;

    @Column(name = "changelog", columnDefinition = "TEXT")
    private String changelog;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "checksum_sha256")
    private String checksumSha256;

    @Column(name = "minimum_platform_version")
    private String minimumPlatformVersion;

    @Column(name = "maximum_platform_version")
    private String maximumPlatformVersion;

    @Column(name = "configuration_schema", columnDefinition = "TEXT")
    private String configurationSchema;

    @Column(name = "default_configuration", columnDefinition = "TEXT")
    private String defaultConfiguration;

    @Column(name = "required_permissions", columnDefinition = "TEXT")
    private String requiredPermissions;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "is_stable")
    @Builder.Default
    private boolean stable = true;

    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public enum VersionStatus {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        YANKED
    }
}
