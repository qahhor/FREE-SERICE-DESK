package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.LiveChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveChatSessionRepository extends JpaRepository<LiveChatSession, String> {

    // Find by visitor
    Optional<LiveChatSession> findByVisitorIdAndStatusIn(String visitorId, List<LiveChatSession.SessionStatus> statuses);

    List<LiveChatSession> findByVisitorIdOrderByCreatedAtDesc(String visitorId);

    // Find by agent
    List<LiveChatSession> findByAssignedAgentIdAndStatus(String agentId, LiveChatSession.SessionStatus status);

    @Query("SELECT s FROM LiveChatSession s WHERE s.assignedAgentId = :agentId " +
            "AND s.status IN ('ACTIVE', 'ON_HOLD') ORDER BY s.lastActivityAt DESC")
    List<LiveChatSession> findActiveSessionsByAgent(@Param("agentId") String agentId);

    // Find by project
    Page<LiveChatSession> findByProjectIdOrderByCreatedAtDesc(String projectId, Pageable pageable);

    @Query("SELECT s FROM LiveChatSession s WHERE s.projectId = :projectId " +
            "AND s.status = 'WAITING' ORDER BY s.createdAt ASC")
    List<LiveChatSession> findWaitingSessionsByProject(@Param("projectId") String projectId);

    // Queue management
    @Query("SELECT s FROM LiveChatSession s WHERE s.status = 'WAITING' " +
            "AND (:department IS NULL OR s.department = :department) " +
            "ORDER BY s.createdAt ASC")
    List<LiveChatSession> findWaitingQueue(@Param("department") String department);

    @Query("SELECT COUNT(s) FROM LiveChatSession s WHERE s.status = 'WAITING' " +
            "AND s.createdAt < :createdAt " +
            "AND (:department IS NULL OR s.department = :department)")
    int countQueuePositionBefore(@Param("createdAt") LocalDateTime createdAt,
                                  @Param("department") String department);

    // Statistics
    @Query("SELECT COUNT(s) FROM LiveChatSession s WHERE s.assignedAgentId = :agentId " +
            "AND s.status IN ('ACTIVE', 'ON_HOLD')")
    int countActiveSessionsByAgent(@Param("agentId") String agentId);

    @Query("SELECT COUNT(s) FROM LiveChatSession s WHERE s.projectId = :projectId " +
            "AND s.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") String projectId,
                                    @Param("status") LiveChatSession.SessionStatus status);

    @Query("SELECT COUNT(s) FROM LiveChatSession s WHERE s.projectId = :projectId " +
            "AND s.createdAt BETWEEN :start AND :end")
    long countByProjectIdAndDateRange(@Param("projectId") String projectId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, s.createdAt, s.assignedAt)) FROM LiveChatSession s " +
            "WHERE s.projectId = :projectId AND s.assignedAt IS NOT NULL " +
            "AND s.createdAt BETWEEN :start AND :end")
    Double averageWaitTime(@Param("projectId") String projectId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    // Cleanup abandoned sessions
    @Query("SELECT s FROM LiveChatSession s WHERE s.status = 'WAITING' " +
            "AND s.lastActivityAt < :threshold")
    List<LiveChatSession> findAbandonedWaitingSessions(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE LiveChatSession s SET s.status = 'ABANDONED' " +
            "WHERE s.status = 'WAITING' AND s.lastActivityAt < :threshold")
    int markAbandonedSessions(@Param("threshold") LocalDateTime threshold);

    // Search
    @Query("SELECT s FROM LiveChatSession s WHERE s.projectId = :projectId " +
            "AND (LOWER(s.visitorName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.visitorEmail) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY s.createdAt DESC")
    Page<LiveChatSession> searchSessions(@Param("projectId") String projectId,
                                          @Param("query") String query,
                                          Pageable pageable);
}
