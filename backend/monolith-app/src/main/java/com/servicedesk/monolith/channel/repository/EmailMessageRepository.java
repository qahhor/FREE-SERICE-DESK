package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.EmailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, String> {

    Optional<EmailMessage> findByMessageId(String messageId);

    List<EmailMessage> findByTicketId(String ticketId);

    Page<EmailMessage> findByChannelId(String channelId, Pageable pageable);

    List<EmailMessage> findByStatus(EmailMessage.Status status);

    @Query("SELECT em FROM EmailMessage em WHERE em.status = 'PENDING' AND em.direction = 'OUTBOUND' ORDER BY em.createdAt ASC")
    List<EmailMessage> findPendingOutboundEmails();

    @Query("SELECT em FROM EmailMessage em WHERE em.status = 'FAILED' AND em.retryCount < :maxRetries ORDER BY em.createdAt ASC")
    List<EmailMessage> findFailedEmailsForRetry(int maxRetries);

    @Query("SELECT em FROM EmailMessage em WHERE em.ticketId = :ticketId ORDER BY em.createdAt DESC")
    List<EmailMessage> findByTicketIdOrderByCreatedAtDesc(String ticketId);

    boolean existsByMessageId(String messageId);

    long countByChannelIdAndDirection(String channelId, EmailMessage.Direction direction);

    @Query("SELECT COUNT(em) FROM EmailMessage em WHERE em.channelId = :channelId AND em.createdAt >= :since")
    long countByChannelIdSince(String channelId, LocalDateTime since);
}
