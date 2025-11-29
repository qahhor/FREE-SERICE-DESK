package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SlaPolicy policy;

    @Column(nullable = false)
    private String field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Operator operator;

    @Column(nullable = false)
    private String value;

    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        IN,
        NOT_IN,
        GREATER_THAN,
        LESS_THAN,
        IS_EMPTY,
        IS_NOT_EMPTY
    }
}
