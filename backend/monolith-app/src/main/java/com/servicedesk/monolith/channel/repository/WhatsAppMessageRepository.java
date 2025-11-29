package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.WhatsAppMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, String> {

    Optional<WhatsAppMessage> findByWhatsappMessageId(String whatsappMessageId);

    boolean existsByWhatsappMessageId(String whatsappMessageId);

    List<WhatsAppMessage> findByTicketIdOrderByCreatedAtDesc(String ticketId);

    List<WhatsAppMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    Page<WhatsAppMessage> findByChannelIdOrderByCreatedAtDesc(String channelId, Pageable pageable);

    List<WhatsAppMessage> findByFromNumberAndChannelIdOrderByCreatedAtDesc(String fromNumber, String channelId);

    @Query("SELECT m FROM WhatsAppMessage m WHERE m.channelId = :channelId " +
            "AND m.fromNumber = :phoneNumber " +
            "ORDER BY m.createdAt DESC")
    List<WhatsAppMessage> findConversationHistory(
            @Param("channelId") String channelId,
            @Param("phoneNumber") String phoneNumber);

    @Query("SELECT m FROM WhatsAppMessage m WHERE m.status = 'PENDING' " +
            "AND m.direction = 'OUTBOUND' " +
            "AND m.retryCount < :maxRetries " +
            "AND m.createdAt > :since")
    List<WhatsAppMessage> findPendingOutboundMessages(
            @Param("maxRetries") int maxRetries,
            @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(m) FROM WhatsAppMessage m WHERE m.channelId = :channelId " +
            "AND m.createdAt BETWEEN :start AND :end")
    long countMessagesByChannelAndDateRange(
            @Param("channelId") String channelId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT m.fromNumber FROM WhatsAppMessage m " +
            "WHERE m.channelId = :channelId AND m.direction = 'INBOUND' " +
            "ORDER BY m.fromNumber")
    List<String> findDistinctContactsByChannel(@Param("channelId") String channelId);
}
