package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_relationships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_asset_id", "child_asset_id", "relationship_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRelationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_asset_id", nullable = false)
    private Asset parentAsset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_asset_id", nullable = false)
    private Asset childAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 50)
    private RelationshipType relationshipType;

    @Column(columnDefinition = "TEXT")
    private String description;

    public enum RelationshipType {
        CONTAINS,       // Parent contains child (e.g., server contains disk)
        DEPENDS_ON,     // Parent depends on child
        CONNECTED_TO,   // Network connection
        INSTALLED_ON,   // Software installed on hardware
        RUNS_ON,        // Service runs on server
        USES,           // Uses another asset
        BACKED_UP_BY,   // Backup relationship
        REPLICATED_TO   // Replication relationship
    }
}
