package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, String> {

    List<AutomationRule> findByTriggerEventAndEnabledTrueOrderByExecutionOrderAsc(AutomationRule.TriggerEvent triggerEvent);

    List<AutomationRule> findByProjectIdAndEnabledTrueOrderByExecutionOrderAsc(String projectId);

    List<AutomationRule> findByProjectId(String projectId);

    List<AutomationRule> findByEnabledTrueOrderByExecutionOrderAsc();
}
