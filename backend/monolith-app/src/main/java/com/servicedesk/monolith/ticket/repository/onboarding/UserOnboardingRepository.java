package com.servicedesk.monolith.ticket.repository.onboarding;

import com.servicedesk.monolith.ticket.entity.onboarding.UserOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOnboardingRepository extends JpaRepository<UserOnboarding, UUID> {

    Optional<UserOnboarding> findByUserId(UUID userId);

    Optional<UserOnboarding> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    List<UserOnboarding> findByTenantId(UUID tenantId);

    @Query("SELECT uo FROM UserOnboarding uo WHERE uo.tenantId = :tenantId AND uo.status = :status")
    List<UserOnboarding> findByTenantIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("status") UserOnboarding.OnboardingStatus status
    );

    @Query("SELECT COUNT(uo) FROM UserOnboarding uo WHERE uo.tenantId = :tenantId AND uo.status = 'COMPLETED'")
    long countCompletedByTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT AVG(uo.currentStep * 100.0 / uo.totalSteps) FROM UserOnboarding uo WHERE uo.tenantId = :tenantId AND uo.totalSteps > 0")
    Double getAverageProgressByTenant(@Param("tenantId") UUID tenantId);

    boolean existsByUserId(UUID userId);
}
