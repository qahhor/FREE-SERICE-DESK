package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_assets_tag", columnList = "asset_tag"),
        @Index(name = "idx_assets_status", columnList = "status"),
        @Index(name = "idx_assets_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset extends BaseEntity {

    @Column(name = "asset_tag", unique = true, nullable = false, length = 50)
    private String assetTag;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AssetStatus status = AssetStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AssetType type = AssetType.HARDWARE;

    // Ownership
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(length = 100)
    private String department;

    @Column(length = 200)
    private String location;

    @Column(name = "cost_center", length = 50)
    private String costCenter;

    // Hardware/Software details
    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    // Software specific
    @Column(length = 50)
    private String version;

    @Column(name = "license_key", length = 255)
    private String licenseKey;

    @Column(name = "license_type", length = 50)
    private String licenseType;

    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    // Financial
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 12, scale = 2)
    private BigDecimal purchaseCost;

    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;

    @Column(name = "depreciation_rate", precision = 5, scale = 2)
    private BigDecimal depreciationRate;

    @Column(name = "current_value", precision = 12, scale = 2)
    private BigDecimal currentValue;

    // Network
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(length = 255)
    private String hostname;

    // Custom fields
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private String customFields;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // Relationships
    @OneToMany(mappedBy = "parentAsset", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssetRelationship> childRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "childAsset", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssetRelationship> parentRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<AssetHistory> history = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "asset_tickets",
            joinColumns = @JoinColumn(name = "asset_id"),
            inverseJoinColumns = @JoinColumn(name = "ticket_id")
    )
    @Builder.Default
    private Set<Ticket> linkedTickets = new HashSet<>();

    public void addHistoryEntry(AssetHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setAsset(this);
    }

    public enum AssetStatus {
        ACTIVE,           // In use
        INACTIVE,         // Not in use but available
        IN_MAINTENANCE,   // Under maintenance
        RESERVED,         // Reserved for future use
        RETIRED,          // No longer in service
        DISPOSED,         // Disposed/sold
        LOST,             // Cannot be located
        STOLEN            // Reported stolen
    }

    public enum AssetType {
        HARDWARE,
        SOFTWARE,
        NETWORK,
        PERIPHERAL,
        MOBILE_DEVICE,
        SERVER,
        STORAGE,
        PRINTER,
        VIRTUAL_MACHINE,
        CLOUD_RESOURCE,
        OTHER
    }
}
