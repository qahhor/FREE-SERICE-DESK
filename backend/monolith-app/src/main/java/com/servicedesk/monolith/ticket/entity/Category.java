package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "color", length = 7)
    @Builder.Default
    private String color = "#6366f1";

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Category> children = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_assignee_id")
    private User defaultAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_team_id")
    private Team defaultTeam;
}
