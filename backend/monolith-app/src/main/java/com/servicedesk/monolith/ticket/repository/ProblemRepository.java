package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID>, JpaSpecificationExecutor<Problem> {

    Optional<Problem> findByProblemNumber(String problemNumber);

    Optional<Problem> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT p FROM Problem p WHERE p.deleted = false")
    Page<Problem> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Problem p WHERE p.deleted = false AND p.status = :status")
    Page<Problem> findByStatus(@Param("status") Problem.ProblemStatus status, Pageable pageable);

    @Query("SELECT p FROM Problem p WHERE p.deleted = false AND p.assignee.id = :assigneeId")
    Page<Problem> findByAssigneeId(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    @Query("SELECT p FROM Problem p WHERE p.deleted = false AND p.reportedBy.id = :reporterId")
    Page<Problem> findByReporterId(@Param("reporterId") UUID reporterId, Pageable pageable);

    @Query("SELECT p FROM Problem p WHERE p.deleted = false AND p.workaroundAvailable = true")
    Page<Problem> findWithWorkaround(Pageable pageable);

    @Query("SELECT p FROM Problem p WHERE p.deleted = false " +
           "AND p.status NOT IN ('CLOSED', 'VERIFIED') AND p.incidentCount > :threshold")
    List<Problem> findHighImpactProblems(@Param("threshold") Integer threshold);

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.deleted = false AND p.status = :status")
    long countByStatus(@Param("status") Problem.ProblemStatus status);

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.deleted = false " +
           "AND p.status IN ('CLOSED', 'VERIFIED') AND p.createdAt BETWEEN :startDate AND :endDate")
    long countResolved(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MAX(CAST(SUBSTRING(p.problemNumber, 4) AS integer)) FROM Problem p " +
           "WHERE p.problemNumber LIKE 'PRB%'")
    Integer findMaxProblemNumber();
}
