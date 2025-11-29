package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.TelegramMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, String> {

    Optional<TelegramMessage> findByTelegramMessageIdAndChatId(Long telegramMessageId, Long chatId);

    List<TelegramMessage> findByTicketId(String ticketId);

    List<TelegramMessage> findByChatId(Long chatId);

    Page<TelegramMessage> findByChannelId(String channelId, Pageable pageable);

    @Query("SELECT tm FROM TelegramMessage tm WHERE tm.ticketId = :ticketId ORDER BY tm.createdAt DESC")
    List<TelegramMessage> findByTicketIdOrderByCreatedAtDesc(String ticketId);

    @Query("SELECT tm FROM TelegramMessage tm WHERE tm.chatId = :chatId AND tm.ticketId IS NOT NULL ORDER BY tm.createdAt DESC")
    List<TelegramMessage> findActiveConversationByChatId(Long chatId);

    boolean existsByTelegramMessageIdAndChatId(Long telegramMessageId, Long chatId);
}
