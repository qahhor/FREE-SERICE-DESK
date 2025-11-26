package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByKey(String key);

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    Optional<Project> findByKeyAndDeletedFalse(String key);

    boolean existsByKey(String key);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND p.status = :status")
    Page<Project> findByStatus(@Param("status") Project.ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND p.status = 'ACTIVE'")
    List<Project> findAllActive();

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId AND p.deleted = false")
    List<Project> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Project p JOIN p.teams t WHERE t.id = :teamId AND p.deleted = false")
    List<Project> findByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.key) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> searchProjects(@Param("search") String search, Pageable pageable);
}
