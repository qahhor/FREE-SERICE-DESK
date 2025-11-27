package com.servicedesk.marketplace.dto;

import com.servicedesk.marketplace.entity.MarketplaceModule;
import com.servicedesk.marketplace.plugin.ModuleCategory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for searching modules
 */
@Data
@Builder
public class ModuleSearchRequest {

    private String query;
    private List<ModuleCategory> categories;
    private List<String> tags;
    private MarketplaceModule.PricingModel pricingModel;
    private Boolean freeOnly;
    private Boolean verifiedOnly;
    private Boolean officialOnly;
    private Double minRating;
    private SortBy sortBy;
    private SortOrder sortOrder;
    private Integer page;
    private Integer size;

    public enum SortBy {
        RELEVANCE,
        POPULARITY,
        RATING,
        NEWEST,
        NAME,
        PRICE
    }

    public enum SortOrder {
        ASC,
        DESC
    }
}
