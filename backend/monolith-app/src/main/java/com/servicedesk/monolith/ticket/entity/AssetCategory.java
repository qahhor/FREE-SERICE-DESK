package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asset_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetCategory extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    @Builder.Default
    private String color = "#6366f1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AssetCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AssetCategory> children = new ArrayList<>();

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Asset> assets = new ArrayList<>();
}
