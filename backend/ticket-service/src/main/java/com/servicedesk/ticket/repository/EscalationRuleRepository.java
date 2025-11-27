package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.EscalationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EscalationRuleRepository extends JpaRepository<EscalationRule, UUID> {

    @Query("SELECT e FROM EscalationRule e WHERE e.deleted = false AND e.isActive = true " +
           "ORDER BY e.executionOrder ASC")
    List<EscalationRule> findAllActiveRules();

    @Query("SELECT e FROM EscalationRule e WHERE e.deleted = false AND e.isActive = true " +
           "AND e.slaPolicy.id = :policyId ORDER BY e.executionOrder ASC")
    List<EscalationRule> findBySlaPolicyId(@Param("policyId") UUID policyId);

    @Query("SELECT e FROM EscalationRule e WHERE e.deleted = false AND e.isActive = true " +
           "AND e.triggerType = :triggerType ORDER BY e.executionOrder ASC")
    List<EscalationRule> findByTriggerType(@Param("triggerType") EscalationRule.TriggerType triggerType);

    @Query("SELECT e FROM EscalationRule e WHERE e.deleted = false AND e.isActive = true " +
           "AND e.triggerType = :triggerType AND e.escalationLevel = :level " +
           "ORDER BY e.executionOrder ASC")
    List<EscalationRule> findByTriggerTypeAndLevel(
            @Param("triggerType") EscalationRule.TriggerType triggerType,
            @Param("level") Integer level);
}
