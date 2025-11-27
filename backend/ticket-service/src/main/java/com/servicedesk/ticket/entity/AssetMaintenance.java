package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "asset_maintenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetMaintenance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false, length = 50)
    private MaintenanceType maintenanceType;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    public enum MaintenanceType {
        PREVENTIVE,     // Regular scheduled maintenance
        CORRECTIVE,     // Fix after failure
        INSPECTION,     // Regular inspection
        UPGRADE,        // Hardware/software upgrade
        CALIBRATION,    // Equipment calibration
        CLEANING        // Physical cleaning
    }

    public enum MaintenanceStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        OVERDUE
    }
}
