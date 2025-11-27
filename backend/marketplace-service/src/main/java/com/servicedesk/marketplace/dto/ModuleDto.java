package com.servicedesk.marketplace.dto;

import com.servicedesk.marketplace.entity.MarketplaceModule;
import com.servicedesk.marketplace.plugin.ModuleCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for marketplace module
 */
@Data
@Builder
public class ModuleDto {

    private UUID id;
    private String moduleId;
    private String name;
    private String description;
    private String shortDescription;
    private ModuleCategory category;
    private String author;
    private String authorUrl;
    private String documentationUrl;
    private String supportUrl;
    private String repositoryUrl;
    private String icon;
    private List<String> screenshots;
    private List<String> tags;
    private MarketplaceModule.PricingModel pricingModel;
    private BigDecimal price;
    private String priceCurrency;
    private Integer trialDays;
    private String latestVersion;
    private String minimumPlatformVersion;
    private List<String> dependencies;
    private Long installCount;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private MarketplaceModule.ModuleStatus status;
    private boolean verified;
    private boolean featured;
    private boolean official;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Installation info (for current tenant)
    private boolean installed;
    private String installedVersion;
    private boolean enabled;
    private boolean updateAvailable;

    public static ModuleDto fromEntity(MarketplaceModule entity) {
        return ModuleDto.builder()
                .id(entity.getId())
                .moduleId(entity.getModuleId())
                .name(entity.getName())
                .description(entity.getDescription())
                .shortDescription(entity.getShortDescription())
                .category(entity.getCategory())
                .author(entity.getAuthor())
                .authorUrl(entity.getAuthorUrl())
                .documentationUrl(entity.getDocumentationUrl())
                .supportUrl(entity.getSupportUrl())
                .repositoryUrl(entity.getRepositoryUrl())
                .icon(entity.getIcon())
                .screenshots(entity.getScreenshots())
                .tags(entity.getTags())
                .pricingModel(entity.getPricingModel())
                .price(entity.getPrice())
                .priceCurrency(entity.getPriceCurrency())
                .trialDays(entity.getTrialDays())
                .latestVersion(entity.getLatestVersion())
                .minimumPlatformVersion(entity.getMinimumPlatformVersion())
                .dependencies(entity.getDependencies())
                .installCount(entity.getInstallCount())
                .averageRating(entity.getAverageRating())
                .reviewCount(entity.getReviewCount())
                .status(entity.getStatus())
                .verified(entity.isVerified())
                .featured(entity.isFeatured())
                .official(entity.isOfficial())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
