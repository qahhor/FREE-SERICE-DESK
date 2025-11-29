package com.servicedesk.monolith.ticket.repository;

import com.servicedesk.monolith.ticket.entity.SlaBreachHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlaBreachHistoryRepository extends JpaRepository<SlaBreachHistory, UUID> {

    @Query("SELECT s FROM SlaBreachHistory s WHERE s.deleted = false AND s.ticket.id = :ticketId")
    List<SlaBreachHistory> findByTicketId(@Param("ticketId") UUID ticketId);

    @Query("SELECT s FROM SlaBreachHistory s WHERE s.deleted = false " +
           "AND s.breachedAt BETWEEN :startDate AND :endDate")
    Page<SlaBreachHistory> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT s FROM SlaBreachHistory s WHERE s.deleted = false " +
           "AND s.breachType = :breachType AND s.breachedAt BETWEEN :startDate AND :endDate")
    List<SlaBreachHistory> findByBreachTypeAndDateRange(
            @Param("breachType") SlaBreachHistory.BreachType breachType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM SlaBreachHistory s WHERE s.deleted = false " +
           "AND s.breachedAt BETWEEN :startDate AND :endDate")
    long countBreachesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(s.breachDurationMinutes) FROM SlaBreachHistory s WHERE s.deleted = false " +
           "AND s.breachedAt BETWEEN :startDate AND :endDate")
    Double avgBreachDurationByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
