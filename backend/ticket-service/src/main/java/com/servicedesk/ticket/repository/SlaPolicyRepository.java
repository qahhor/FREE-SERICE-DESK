package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, String> {

    List<SlaPolicy> findByProjectIdAndEnabledTrueOrderByPriorityOrderAsc(String projectId);

    List<SlaPolicy> findByEnabledTrueOrderByPriorityOrderAsc();

    Optional<SlaPolicy> findByIsDefaultTrueAndEnabledTrue();

    List<SlaPolicy> findByProjectId(String projectId);
}
