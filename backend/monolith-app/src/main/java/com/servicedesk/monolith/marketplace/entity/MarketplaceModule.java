package com.servicedesk.monolith.marketplace.entity;

import com.servicedesk.monolith.marketplace.plugin.ModuleCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a module available in the marketplace
 */
@Entity
@Table(name = "marketplace_modules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceModule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "module_id", nullable = false, unique = true)
    private String moduleId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleCategory category;

    @Column(nullable = false)
    private String author;

    @Column(name = "author_url")
    private String authorUrl;

    @Column(name = "documentation_url")
    private String documentationUrl;

    @Column(name = "support_url")
    private String supportUrl;

    @Column(name = "repository_url")
    private String repositoryUrl;

    @Column(columnDefinition = "TEXT")
    private String icon;

    @ElementCollection
    @CollectionTable(name = "module_screenshots", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "screenshot_url")
    private List<String> screenshots = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "module_tags", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model", nullable = false)
    @Builder.Default
    private PricingModel pricingModel = PricingModel.FREE;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal price;

    @Column(name = "price_currency")
    @Builder.Default
    private String priceCurrency = "USD";

    @Column(name = "trial_days")
    @Builder.Default
    private Integer trialDays = 0;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ModuleVersion> versions = new ArrayList<>();

    @Column(name = "latest_version")
    private String latestVersion;

    @Column(name = "minimum_platform_version")
    private String minimumPlatformVersion;

    @ElementCollection
    @CollectionTable(name = "module_dependencies", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "dependency_module_id")
    private List<String> dependencies = new ArrayList<>();

    @Column(name = "install_count")
    @Builder.Default
    private Long installCount = 0L;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private java.math.BigDecimal averageRating = java.math.BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModuleStatus status = ModuleStatus.DRAFT;

    @Column(name = "is_verified")
    @Builder.Default
    private boolean verified = false;

    @Column(name = "is_featured")
    @Builder.Default
    private boolean featured = false;

    @Column(name = "is_official")
    @Builder.Default
    private boolean official = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum PricingModel {
        FREE,
        ONE_TIME,
        SUBSCRIPTION_MONTHLY,
        SUBSCRIPTION_YEARLY,
        USAGE_BASED,
        CONTACT_US
    }

    public enum ModuleStatus {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        SUSPENDED,
        DEPRECATED
    }
}
