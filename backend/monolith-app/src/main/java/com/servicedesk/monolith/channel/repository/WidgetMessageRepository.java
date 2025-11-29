package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.WidgetMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WidgetMessageRepository extends JpaRepository<WidgetMessage, String> {

    List<WidgetMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    @Modifying
    @Query("UPDATE WidgetMessage m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.isRead = false")
    void markAllAsRead(String conversationId);

    long countByConversationIdAndIsReadFalse(String conversationId);
}
