package com.servicedesk.marketplace.repository;

import com.servicedesk.marketplace.entity.ModuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleVersionRepository extends JpaRepository<ModuleVersion, UUID> {

    @Query("SELECT mv FROM ModuleVersion mv WHERE mv.module.moduleId = :moduleId ORDER BY mv.createdAt DESC")
    List<ModuleVersion> findByModuleId(@Param("moduleId") String moduleId);

    @Query("SELECT mv FROM ModuleVersion mv WHERE mv.module.moduleId = :moduleId AND mv.version = :version")
    Optional<ModuleVersion> findByModuleIdAndVersion(@Param("moduleId") String moduleId, @Param("version") String version);

    @Query("SELECT mv FROM ModuleVersion mv WHERE mv.module.moduleId = :moduleId AND mv.status = 'PUBLISHED' ORDER BY mv.createdAt DESC")
    List<ModuleVersion> findPublishedVersions(@Param("moduleId") String moduleId);

    @Query("SELECT mv FROM ModuleVersion mv WHERE mv.module.moduleId = :moduleId AND mv.status = 'PUBLISHED' AND mv.stable = true ORDER BY mv.createdAt DESC")
    Optional<ModuleVersion> findLatestStableVersion(@Param("moduleId") String moduleId);
}
