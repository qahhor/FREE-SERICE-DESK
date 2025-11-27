package com.servicedesk.channel.service;

import com.servicedesk.channel.dto.WidgetConversationDto;
import com.servicedesk.channel.dto.WidgetMessageDto;
import com.servicedesk.channel.entity.WidgetConversation;
import com.servicedesk.channel.entity.WidgetMessage;
import com.servicedesk.channel.repository.WidgetConversationRepository;
import com.servicedesk.channel.repository.WidgetMessageRepository;
import com.servicedesk.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetConversationRepository conversationRepository;
    private final WidgetMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public WidgetConversationDto createConversation(CreateConversationRequest request) {
        // Check for existing active conversation
        WidgetConversation existing = conversationRepository
                .findByVisitorIdAndStatusNot(request.getVisitorId(), WidgetConversation.ConversationStatus.CLOSED)
                .orElse(null);

        if (existing != null) {
            return toDto(existing);
        }

        WidgetConversation conversation = WidgetConversation.builder()
                .projectId(request.getProjectId())
                .visitorId(request.getVisitorId())
                .visitorName(request.getVisitorName())
                .visitorEmail(request.getVisitorEmail())
                .visitorPhone(request.getVisitorPhone())
                .pageUrl(request.getPageUrl())
                .pageTitle(request.getPageTitle())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .status(WidgetConversation.ConversationStatus.ACTIVE)
                .build();

        conversation = conversationRepository.save(conversation);

        // Notify agents about new conversation
        notifyAgents(conversation, "new_conversation");

        log.info("Widget conversation created: {} for visitor {}", conversation.getId(), request.getVisitorId());

        return toDto(conversation);
    }

    @Transactional
    public WidgetMessageDto addMessage(String conversationId, String visitorId, String content,
                                        WidgetMessage.SenderType senderType, String senderId, String senderName) {
        WidgetConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Verify visitor owns the conversation
        if (senderType == WidgetMessage.SenderType.VISITOR && !conversation.getVisitorId().equals(visitorId)) {
            throw new SecurityException("Visitor does not own this conversation");
        }

        WidgetMessage message = WidgetMessage.builder()
                .conversation(conversation)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName != null ? senderName :
                        (senderType == WidgetMessage.SenderType.VISITOR ? conversation.getVisitorName() : "Agent"))
                .content(content)
                .messageType(WidgetMessage.MessageType.TEXT)
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        WidgetMessageDto dto = toMessageDto(message);

        // Send via WebSocket to visitor
        messagingTemplate.convertAndSend("/topic/widget/" + conversationId, dto);

        // Send via WebSocket to agents
        messagingTemplate.convertAndSend("/topic/agent/conversations/" + conversationId, dto);

        // If visitor message, notify agents
        if (senderType == WidgetMessage.SenderType.VISITOR) {
            notifyAgents(conversation, "new_message");
        }

        return dto;
    }

    public WidgetConversationDto getConversation(String conversationId, String visitorId) {
        WidgetConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getVisitorId().equals(visitorId)) {
            throw new SecurityException("Visitor does not own this conversation");
        }

        return toDto(conversation);
    }

    public List<WidgetMessageDto> getMessages(String conversationId, String visitorId) {
        WidgetConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getVisitorId().equals(visitorId)) {
            throw new SecurityException("Visitor does not own this conversation");
        }

        return conversation.getMessages().stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignToAgent(String conversationId, String agentId) {
        WidgetConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setAssignedAgentId(agentId);
        conversation.setStatus(WidgetConversation.ConversationStatus.ASSIGNED);
        conversationRepository.save(conversation);

        // Add system message
        addMessage(conversationId, null, "An agent has joined the conversation",
                WidgetMessage.SenderType.SYSTEM, null, "System");
    }

    @Transactional
    public void closeConversation(String conversationId, Integer rating, String feedback) {
        WidgetConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setStatus(WidgetConversation.ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());
        if (rating != null) {
            conversation.setRating(rating);
        }
        if (feedback != null) {
            conversation.setFeedback(feedback);
        }
        conversationRepository.save(conversation);

        // Add system message
        addMessage(conversationId, null, "Conversation has been closed",
                WidgetMessage.SenderType.SYSTEM, null, "System");
    }

    @Transactional
    public void sendTypingIndicator(String conversationId, String visitorId, boolean isTyping) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "typing");
        payload.put("conversationId", conversationId);
        payload.put("isTyping", isTyping);
        payload.put("isVisitor", visitorId != null);

        messagingTemplate.convertAndSend("/topic/widget/" + conversationId, payload);
        messagingTemplate.convertAndSend("/topic/agent/conversations/" + conversationId, payload);
    }

    public List<WidgetConversationDto> getActiveConversations(String projectId) {
        return conversationRepository.findByProjectIdAndStatusNotOrderByLastMessageAtDesc(
                        projectId, WidgetConversation.ConversationStatus.CLOSED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<WidgetConversationDto> getAgentConversations(String agentId) {
        return conversationRepository.findByAssignedAgentIdAndStatusNot(
                        agentId, WidgetConversation.ConversationStatus.CLOSED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void notifyAgents(WidgetConversation conversation, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", eventType);
        event.put("conversation", toDto(conversation));

        rabbitTemplate.convertAndSend("widget.notifications", event);
        messagingTemplate.convertAndSend("/topic/agent/widget-events", event);
    }

    private WidgetConversationDto toDto(WidgetConversation conversation) {
        return WidgetConversationDto.builder()
                .id(conversation.getId())
                .projectId(conversation.getProjectId())
                .visitorId(conversation.getVisitorId())
                .visitorName(conversation.getVisitorName())
                .visitorEmail(conversation.getVisitorEmail())
                .ticketId(conversation.getTicketId())
                .assignedAgentId(conversation.getAssignedAgentId())
                .status(conversation.getStatus())
                .pageUrl(conversation.getPageUrl())
                .pageTitle(conversation.getPageTitle())
                .locale(conversation.getLocale())
                .lastMessageAt(conversation.getLastMessageAt())
                .rating(conversation.getRating())
                .messages(conversation.getMessages().stream()
                        .map(this::toMessageDto)
                        .collect(Collectors.toList()))
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private WidgetMessageDto toMessageDto(WidgetMessage message) {
        return WidgetMessageDto.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderType(message.getSenderType())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentName(message.getAttachmentName())
                .isRead(message.getIsRead())
                .timestamp(message.getCreatedAt())
                .build();
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class CreateConversationRequest {
        private String projectId;
        private String visitorId;
        private String visitorName;
        private String visitorEmail;
        private String visitorPhone;
        private String pageUrl;
        private String pageTitle;
        private String userAgent;
        private String ipAddress;
        private String locale;
    }
}
