package com.servicedesk.monolith.ticket.dto;

import com.servicedesk.monolith.ticket.entity.Asset;
import com.servicedesk.monolith.ticket.entity.AssetMaintenance;
import com.servicedesk.monolith.ticket.entity.AssetRelationship;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AssetDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String assetTag; // Auto-generated if not provided
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private String categoryId;
        private Asset.AssetStatus status;
        private Asset.AssetType type;
        private String ownerId;
        private String department;
        private String location;
        private String costCenter;
        private String manufacturer;
        private String model;
        private String serialNumber;
        private String version;
        private String licenseKey;
        private String licenseType;
        private LocalDate licenseExpiry;
        private LocalDate purchaseDate;
        private BigDecimal purchaseCost;
        private LocalDate warrantyExpiry;
        private BigDecimal depreciationRate;
        private String ipAddress;
        private String macAddress;
        private String hostname;
        private Map<String, Object> customFields;
        private String projectId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String description;
        private String categoryId;
        private Asset.AssetStatus status;
        private Asset.AssetType type;
        private String ownerId;
        private String department;
        private String location;
        private String costCenter;
        private String manufacturer;
        private String model;
        private String serialNumber;
        private String version;
        private String licenseKey;
        private String licenseType;
        private LocalDate licenseExpiry;
        private LocalDate purchaseDate;
        private BigDecimal purchaseCost;
        private LocalDate warrantyExpiry;
        private BigDecimal depreciationRate;
        private BigDecimal currentValue;
        private String ipAddress;
        private String macAddress;
        private String hostname;
        private Map<String, Object> customFields;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String assetTag;
        private String name;
        private String description;
        private CategorySummary category;
        private Asset.AssetStatus status;
        private Asset.AssetType type;
        private UserSummary owner;
        private String department;
        private String location;
        private String costCenter;
        private String manufacturer;
        private String model;
        private String serialNumber;
        private String version;
        private String licenseKey;
        private String licenseType;
        private LocalDate licenseExpiry;
        private LocalDate purchaseDate;
        private BigDecimal purchaseCost;
        private LocalDate warrantyExpiry;
        private BigDecimal depreciationRate;
        private BigDecimal currentValue;
        private String ipAddress;
        private String macAddress;
        private String hostname;
        private Map<String, Object> customFields;
        private String projectId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        // Computed fields
        private Boolean warrantyActive;
        private Boolean licenseActive;
        private Integer relatedTicketsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private String id;
        private String assetTag;
        private String name;
        private String categoryName;
        private Asset.AssetStatus status;
        private Asset.AssetType type;
        private String ownerName;
        private String location;
        private LocalDate warrantyExpiry;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String id;
        private String name;
        private String icon;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private String id;
        private String email;
        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private String id;
        private String name;
        private String description;
        private String icon;
        private String color;
        private String parentId;
        private Integer sortOrder;
        private Long assetCount;
        private List<CategoryResponse> children;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipRequest {
        private String childAssetId;
        private AssetRelationship.RelationshipType relationshipType;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipResponse {
        private String id;
        private String parentAssetId;
        private String parentAssetName;
        private String childAssetId;
        private String childAssetName;
        private AssetRelationship.RelationshipType relationshipType;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceRequest {
        private AssetMaintenance.MaintenanceType maintenanceType;
        private LocalDate scheduledDate;
        private String assignedToId;
        private String notes;
        private BigDecimal cost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceResponse {
        private String id;
        private String assetId;
        private String assetName;
        private AssetMaintenance.MaintenanceType maintenanceType;
        private LocalDate scheduledDate;
        private LocalDate completedDate;
        private AssetMaintenance.MaintenanceStatus status;
        private String assignedToId;
        private String assignedToName;
        private String notes;
        private BigDecimal cost;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryResponse {
        private String id;
        private String action;
        private String fieldName;
        private String oldValue;
        private String newValue;
        private String description;
        private String userId;
        private String userName;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetMetrics {
        private Long totalAssets;
        private Long activeAssets;
        private Long inMaintenanceAssets;
        private Long retiredAssets;
        private BigDecimal totalPurchaseCost;
        private BigDecimal totalCurrentValue;
        private BigDecimal depreciation;
        private Long warrantyExpiringIn30Days;
        private Long licenseExpiringIn30Days;
        private Long overdueMaintenance;
        private Map<String, Long> byType;
        private Map<String, Long> byStatus;
        private Map<String, Long> byCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkTicketRequest {
        private String ticketId;
        private String impactType; // AFFECTED, CAUSED_BY, RELATED_TO
        private String notes;
    }
}
