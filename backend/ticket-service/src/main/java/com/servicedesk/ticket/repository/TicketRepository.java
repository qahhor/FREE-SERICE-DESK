package com.servicedesk.ticket.repository;

import com.servicedesk.ticket.entity.Ticket;
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
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByIdAndDeletedFalse(UUID id);

    Optional<Ticket> findByTicketNumberAndDeletedFalse(String ticketNumber);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.assignee.id = :assigneeId")
    Page<Ticket> findByAssigneeId(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.requester.id = :requesterId")
    Page<Ticket> findByRequesterId(@Param("requesterId") UUID requesterId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.project.id = :projectId")
    Page<Ticket> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.team.id = :teamId")
    Page<Ticket> findByTeamId(@Param("teamId") UUID teamId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.status = :status")
    Page<Ticket> findByStatus(@Param("status") Ticket.TicketStatus status, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.assignee IS NULL " +
           "AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED')")
    Page<Ticket> findUnassignedTickets(Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.dueDate < :now " +
           "AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED')")
    List<Ticket> findOverdueTickets(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = false AND t.status = :status")
    long countByStatus(@Param("status") Ticket.TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = false AND t.assignee.id = :assigneeId " +
           "AND t.status NOT IN ('CLOSED', 'CANCELLED')")
    long countOpenTicketsByAssignee(@Param("assigneeId") UUID assigneeId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = false AND t.project.id = :projectId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    long countByProjectAndDateRange(
            @Param("projectId") UUID projectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT MAX(CAST(SUBSTRING(t.ticketNumber, LENGTH(:prefix) + 1) AS integer)) " +
           "FROM Ticket t WHERE t.ticketNumber LIKE CONCAT(:prefix, '%')")
    Integer findMaxTicketNumberByPrefix(@Param("prefix") String prefix);

    @Query("SELECT t FROM Ticket t JOIN t.watchers w WHERE w.id = :userId AND t.deleted = false")
    Page<Ticket> findWatchedByUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.status NOT IN ('CLOSED', 'CANCELLED') " +
           "AND t.updatedAt < :lastActivityBefore")
    List<Ticket> findStaleTickets(@Param("lastActivityBefore") LocalDateTime lastActivityBefore);

    // SLA-related queries
    @Query("SELECT t FROM Ticket t WHERE t.deleted = false " +
           "AND t.firstResponseDue IS NOT NULL AND t.firstResponseDue < :beforeTime " +
           "AND t.firstResponseAt IS NULL " +
           "AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED')")
    List<Ticket> findTicketsWithFirstResponseDueBefore(@Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false " +
           "AND t.resolutionDue IS NOT NULL AND t.resolutionDue < :beforeTime " +
           "AND t.resolvedAt IS NULL " +
           "AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED')")
    List<Ticket> findTicketsWithResolutionDueBefore(@Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false " +
           "AND ((t.firstResponseDue IS NOT NULL AND t.firstResponseDue < :now AND t.firstResponseAt IS NULL) " +
           "OR (t.resolutionDue IS NOT NULL AND t.resolutionDue < :now AND t.resolvedAt IS NULL)) " +
           "AND t.status NOT IN ('CLOSED', 'CANCELLED', 'RESOLVED')")
    List<Ticket> findTicketsWithSlaBreached(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false AND t.slaBreached = true")
    Page<Ticket> findBreachedTickets(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = false AND t.slaBreached = true " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    long countBreachedTickets(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = false " +
           "AND t.firstResponseAt IS NOT NULL AND t.firstResponseDue IS NOT NULL " +
           "AND t.firstResponseAt <= t.firstResponseDue " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    long countFirstResponseWithinSla(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = false " +
           "AND t.resolvedAt IS NOT NULL AND t.resolutionDue IS NOT NULL " +
           "AND t.resolvedAt <= t.resolutionDue " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    long countResolutionWithinSla(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false " +
           "AND t.slaPolicy.id = :policyId")
    Page<Ticket> findBySlaPolicyId(@Param("policyId") UUID policyId, Pageable pageable);
}
