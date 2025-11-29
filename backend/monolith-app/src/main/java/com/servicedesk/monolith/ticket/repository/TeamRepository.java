package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.Team;
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
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT t FROM Team t WHERE t.deleted = false")
    List<Team> findAllActive();

    @Query("SELECT t FROM Team t WHERE t.deleted = false")
    Page<Team> findAllActive(Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.manager.id = :managerId AND t.deleted = false")
    List<Team> findByManagerId(@Param("managerId") UUID managerId);

    @Query("SELECT t FROM Team t JOIN t.projects p WHERE p.id = :projectId AND t.deleted = false")
    List<Team> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT t FROM Team t WHERE t.deleted = false AND " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Team> searchTeams(@Param("search") String search, Pageable pageable);
}
