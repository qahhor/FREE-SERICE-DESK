package com.servicedesk.channel.repository;

import com.servicedesk.channel.entity.LiveChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveChatMessageRepository extends JpaRepository<LiveChatMessage, String> {

    List<LiveChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    Page<LiveChatMessage> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    @Query("SELECT m FROM LiveChatMessage m WHERE m.session.id = :sessionId " +
            "ORDER BY m.createdAt DESC")
    List<LiveChatMessage> findRecentMessages(@Param("sessionId") String sessionId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM LiveChatMessage m WHERE m.session.id = :sessionId " +
            "AND m.senderType = :senderType")
    long countBySenderType(@Param("sessionId") String sessionId,
                           @Param("senderType") LiveChatMessage.SenderType senderType);

    @Query("SELECT m FROM LiveChatMessage m WHERE m.session.id = :sessionId " +
            "AND m.isRead = false AND m.senderType = :senderType")
    List<LiveChatMessage> findUnreadMessages(@Param("sessionId") String sessionId,
                                              @Param("senderType") LiveChatMessage.SenderType senderType);

    @Modifying
    @Query("UPDATE LiveChatMessage m SET m.isRead = true, m.readAt = :readAt " +
            "WHERE m.session.id = :sessionId AND m.isRead = false " +
            "AND m.senderType = :senderType")
    int markMessagesAsRead(@Param("sessionId") String sessionId,
                           @Param("senderType") LiveChatMessage.SenderType senderType,
                           @Param("readAt") LocalDateTime readAt);

    @Query("SELECT COUNT(m) FROM LiveChatMessage m WHERE m.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT m FROM LiveChatMessage m WHERE m.session.id = :sessionId " +
            "AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<LiveChatMessage> findMessagesSince(@Param("sessionId") String sessionId,
                                             @Param("since") LocalDateTime since);
}
