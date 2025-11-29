package com.servicedesk.monolith.ticket.repository.onboarding;

import com.servicedesk.monolith.ticket.entity.onboarding.OnboardingStep;
import com.servicedesk.monolith.ticket.entity.onboarding.UserOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingStepRepository extends JpaRepository<OnboardingStep, UUID> {

    Optional<OnboardingStep> findByStepId(String stepId);

    @Query("""
        SELECT os FROM OnboardingStep os
        WHERE os.targetRole = :role
        AND os.active = true
        ORDER BY os.displayOrder
    """)
    List<OnboardingStep> findByTargetRoleOrderByDisplayOrder(
            @Param("role") UserOnboarding.UserRole role
    );

    @Query("""
        SELECT os FROM OnboardingStep os
        WHERE os.targetRole = :role
        AND os.active = true
        AND os.type = :type
        ORDER BY os.displayOrder
    """)
    List<OnboardingStep> findByTargetRoleAndType(
            @Param("role") UserOnboarding.UserRole role,
            @Param("type") OnboardingStep.StepType type
    );

    @Query("SELECT COUNT(os) FROM OnboardingStep os WHERE os.targetRole = :role AND os.active = true AND os.required = true")
    int countRequiredStepsByRole(@Param("role") UserOnboarding.UserRole role);

    List<OnboardingStep> findByActiveTrue();
}
