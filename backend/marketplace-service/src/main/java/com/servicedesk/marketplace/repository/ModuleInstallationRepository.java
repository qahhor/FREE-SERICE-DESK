package com.servicedesk.marketplace.repository;

import com.servicedesk.marketplace.entity.ModuleInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleInstallationRepository extends JpaRepository<ModuleInstallation, UUID> {

    Optional<ModuleInstallation> findByTenantIdAndModuleId(UUID tenantId, String moduleId);

    List<ModuleInstallation> findByTenantId(UUID tenantId);

    List<ModuleInstallation> findByTenantIdAndEnabled(UUID tenantId, boolean enabled);

    List<ModuleInstallation> findByModuleId(String moduleId);

    boolean existsByTenantIdAndModuleId(UUID tenantId, String moduleId);

    @Query("SELECT mi FROM ModuleInstallation mi WHERE mi.tenantId = :tenantId AND mi.status = 'ACTIVE' AND mi.enabled = true")
    List<ModuleInstallation> findActiveInstallations(@Param("tenantId") UUID tenantId);

    @Query("SELECT mi FROM ModuleInstallation mi WHERE mi.autoUpdate = true AND mi.status = 'ACTIVE'")
    List<ModuleInstallation> findAutoUpdateEnabled();

    @Query("SELECT mi FROM ModuleInstallation mi WHERE mi.trialExpiresAt IS NOT NULL AND mi.trialExpiresAt < :now AND mi.status = 'ACTIVE'")
    List<ModuleInstallation> findExpiredTrials(@Param("now") Instant now);

    @Query("SELECT mi FROM ModuleInstallation mi WHERE mi.licenseExpiresAt IS NOT NULL AND mi.licenseExpiresAt < :now AND mi.status = 'ACTIVE'")
    List<ModuleInstallation> findExpiredLicenses(@Param("now") Instant now);

    @Query("SELECT COUNT(mi) FROM ModuleInstallation mi WHERE mi.moduleId = :moduleId AND mi.status IN ('ACTIVE', 'DISABLED')")
    long countInstallations(@Param("moduleId") String moduleId);

    @Query("SELECT mi.moduleId, COUNT(mi) FROM ModuleInstallation mi WHERE mi.status = 'ACTIVE' GROUP BY mi.moduleId ORDER BY COUNT(mi) DESC")
    List<Object[]> findMostInstalledModules();
}
