package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rca_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RcaTemplate extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 30)
    private TemplateType templateType;

    @Column(columnDefinition = "jsonb")
    private String questions; // JSON structure

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    public enum TemplateType {
        FIVE_WHYS,
        FISHBONE,
        FAULT_TREE,
        TIMELINE,
        PARETO,
        CUSTOM
    }
}
