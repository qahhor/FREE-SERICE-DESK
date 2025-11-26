package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "key", nullable = false, unique = true, length = 10)
    private String key;

    @Column(name = "description")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private User lead;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> members = new HashSet<>();

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Team> teams = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    public enum ProjectStatus {
        ACTIVE,
        ARCHIVED,
        SUSPENDED
    }
}
