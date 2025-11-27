package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sla_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_id")
    private String projectId;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "priority_order")
    private Integer priorityOrder = 0;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlaTarget> targets = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlaCondition> conditions = new ArrayList<>();

    // Business hours configuration
    @Column(name = "business_hours_only")
    private Boolean businessHoursOnly = true;

    @Column(name = "business_hours_start")
    private String businessHoursStart = "09:00";

    @Column(name = "business_hours_end")
    private String businessHoursEnd = "18:00";

    @Column(name = "business_days")
    private String businessDays = "1,2,3,4,5"; // Monday to Friday

    @Column(name = "timezone")
    private String timezone = "UTC";
}
