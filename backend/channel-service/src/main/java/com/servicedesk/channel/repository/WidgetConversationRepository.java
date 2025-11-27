package com.servicedesk.channel.repository;

import com.servicedesk.channel.entity.WidgetConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WidgetConversationRepository extends JpaRepository<WidgetConversation, String> {

    Optional<WidgetConversation> findByVisitorIdAndStatusNot(String visitorId, WidgetConversation.ConversationStatus status);

    List<WidgetConversation> findByProjectIdAndStatusNotOrderByLastMessageAtDesc(
            String projectId, WidgetConversation.ConversationStatus status);

    List<WidgetConversation> findByAssignedAgentIdAndStatusNot(
            String agentId, WidgetConversation.ConversationStatus status);

    List<WidgetConversation> findByProjectIdAndStatus(
            String projectId, WidgetConversation.ConversationStatus status);

    long countByProjectIdAndStatus(String projectId, WidgetConversation.ConversationStatus status);
}
