package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "automation_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AutomationRule rule;

    @Column(nullable = false)
    private String field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlaCondition.Operator operator;

    @Column(nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "logic_operator")
    private LogicOperator logicOperator = LogicOperator.AND;

    public enum LogicOperator {
        AND,
        OR
    }
}
