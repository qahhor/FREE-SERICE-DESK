package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.ChangeRequest;
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
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, UUID>, JpaSpecificationExecutor<ChangeRequest> {

    Optional<ChangeRequest> findByChangeNumber(String changeNumber);

    Optional<ChangeRequest> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false")
    Page<ChangeRequest> findAllActive(Pageable pageable);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false AND c.status = :status")
    Page<ChangeRequest> findByStatus(@Param("status") ChangeRequest.ChangeStatus status, Pageable pageable);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false AND c.changeType = :type")
    Page<ChangeRequest> findByType(@Param("type") ChangeRequest.ChangeType type, Pageable pageable);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false AND c.requester.id = :requesterId")
    Page<ChangeRequest> findByRequesterId(@Param("requesterId") UUID requesterId, Pageable pageable);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false AND c.assignee.id = :assigneeId")
    Page<ChangeRequest> findByAssigneeId(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false " +
           "AND c.scheduledStart BETWEEN :startDate AND :endDate")
    List<ChangeRequest> findScheduledInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM ChangeRequest c WHERE c.deleted = false " +
           "AND c.status = 'PENDING_APPROVAL'")
    Page<ChangeRequest> findPendingApproval(Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChangeRequest c WHERE c.deleted = false AND c.status = :status")
    long countByStatus(@Param("status") ChangeRequest.ChangeStatus status);

    @Query("SELECT COUNT(c) FROM ChangeRequest c WHERE c.deleted = false " +
           "AND c.success = true AND c.createdAt BETWEEN :startDate AND :endDate")
    long countSuccessful(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(c) FROM ChangeRequest c WHERE c.deleted = false " +
           "AND c.success = false AND c.createdAt BETWEEN :startDate AND :endDate")
    long countFailed(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MAX(CAST(SUBSTRING(c.changeNumber, 4) AS integer)) FROM ChangeRequest c " +
           "WHERE c.changeNumber LIKE 'CHG%'")
    Integer findMaxChangeNumber();
}
