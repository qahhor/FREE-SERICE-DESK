package com.servicedesk.monolith.channel.service;

import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.monolith.channel.dto.LiveChatDto;
import com.servicedesk.monolith.channel.dto.LiveChatDto.*;
import com.servicedesk.monolith.channel.entity.LiveChatMessage;
import com.servicedesk.monolith.channel.entity.LiveChatSession;
import com.servicedesk.monolith.channel.repository.LiveChatMessageRepository;
import com.servicedesk.monolith.channel.repository.LiveChatSessionRepository;
import com.servicedesk.common.exception.ResourceNotFoundException;
import com.servicedesk.common.exception.ServiceDeskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LiveChatService {

    private final LiveChatSessionRepository sessionRepository;
    private final LiveChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    // In-memory store for agent availability (in production, use Redis)
    private final Map<String, AgentStatus> agentStatuses = new ConcurrentHashMap<>();

    // In-memory store for typing indicators
    private final Map<String, Set<String>> typingUsers = new ConcurrentHashMap<>();

    // ==================== Session Management ====================

    @Transactional
    public ChatSession startChat(StartChatRequest request) {
        // Check for existing active session
        Optional<LiveChatSession> existing = sessionRepository.findByVisitorIdAndStatusIn(
                request.getVisitorId(),
                List.of(LiveChatSession.SessionStatus.WAITING, LiveChatSession.SessionStatus.ACTIVE)
        );

        if (existing.isPresent()) {
            return toSessionDto(existing.get());
        }

        // Create new session
        LiveChatSession session = LiveChatSession.builder()
                .projectId(request.getProjectId())
                .visitorId(request.getVisitorId())
                .visitorName(request.getVisitorName())
                .visitorEmail(request.getVisitorEmail())
                .visitorPhone(request.getVisitorPhone())
                .department(request.getDepartment())
                .pageUrl(request.getPageUrl())
                .pageTitle(request.getPageTitle())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .status(LiveChatSession.SessionStatus.WAITING)
                .startedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();

        if (request.getCustomFields() != null) {
            try {
                session.setCustomFields(objectMapper.writeValueAsString(request.getCustomFields()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize custom fields", e);
            }
        }

        session = sessionRepository.save(session);

        // Add initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            addMessage(session, LiveChatMessage.SenderType.VISITOR, request.getVisitorId(),
                    request.getVisitorName(), request.getInitialMessage(), LiveChatMessage.MessageType.TEXT);
        }

        // Add system message
        addSystemMessage(session, "Chat started. Please wait for an agent to join.");

        // Calculate queue position
        int queuePosition = sessionRepository.countQueuePositionBefore(session.getCreatedAt(), session.getDepartment()) + 1;
        session.setQueuePosition(queuePosition);
        session = sessionRepository.save(session);

        // Notify agents about new chat
        notifyAgents(session, EventType.CHAT_STARTED);

        // Try auto-assign
        tryAutoAssign(session);

        log.info("Live chat session started: {} for visitor {}", session.getId(), request.getVisitorId());

        return toSessionDto(session);
    }

    @Transactional
    public ChatSession assignAgent(AssignAgentRequest request) {
        LiveChatSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (session.getStatus() != LiveChatSession.SessionStatus.WAITING) {
            throw new ServiceDeskException("Chat session is not waiting for assignment");
        }

        // Check agent availability
        AgentStatus agentStatus = agentStatuses.get(request.getAgentId());
        if (agentStatus == null || agentStatus.getStatus() != AgentAvailability.ONLINE) {
            throw new ServiceDeskException("Agent is not available");
        }

        // Assign agent
        session.setAssignedAgentId(request.getAgentId());
        session.setAssignedAgentName(agentStatus.getAgentName());
        session.setStatus(LiveChatSession.SessionStatus.ACTIVE);
        session.setAssignedAt(LocalDateTime.now());
        session.setQueuePosition(null);
        session = sessionRepository.save(session);

        // Add system message
        addSystemMessage(session, agentStatus.getAgentName() + " has joined the chat.");

        // Notify visitor
        notifyVisitor(session, EventType.CHAT_ASSIGNED);

        // Notify agents
        notifyAgents(session, EventType.AGENT_JOINED);

        // Update queue positions for waiting sessions
        updateQueuePositions(session.getDepartment());

        log.info("Agent {} assigned to chat session {}", request.getAgentId(), session.getId());

        return toSessionDto(session);
    }

    @Transactional
    public ChatSession transferChat(TransferChatRequest request) {
        LiveChatSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (session.getStatus() != LiveChatSession.SessionStatus.ACTIVE) {
            throw new ServiceDeskException("Chat session is not active");
        }

        String previousAgent = session.getAssignedAgentName();

        if (request.getToAgentId() != null) {
            // Transfer to specific agent
            AgentStatus agentStatus = agentStatuses.get(request.getToAgentId());
            if (agentStatus == null || agentStatus.getStatus() != AgentAvailability.ONLINE) {
                throw new ServiceDeskException("Target agent is not available");
            }

            session.setAssignedAgentId(request.getToAgentId());
            session.setAssignedAgentName(agentStatus.getAgentName());

            addSystemMessage(session, String.format("Chat transferred from %s to %s. Reason: %s",
                    previousAgent, agentStatus.getAgentName(),
                    request.getReason() != null ? request.getReason() : "No reason provided"));
        } else if (request.getToDepartment() != null) {
            // Transfer to department queue
            session.setAssignedAgentId(null);
            session.setAssignedAgentName(null);
            session.setDepartment(request.getToDepartment());
            session.setStatus(LiveChatSession.SessionStatus.WAITING);

            addSystemMessage(session, String.format("Chat transferred to %s department. Reason: %s",
                    request.getToDepartment(),
                    request.getReason() != null ? request.getReason() : "No reason provided"));
        }

        session = sessionRepository.save(session);

        // Notify
        notifyVisitor(session, EventType.CHAT_TRANSFERRED);
        notifyAgents(session, EventType.CHAT_TRANSFERRED);

        log.info("Chat session {} transferred", session.getId());

        return toSessionDto(session);
    }

    @Transactional
    public ChatSession endChat(EndChatRequest request) {
        LiveChatSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (session.getStatus() == LiveChatSession.SessionStatus.CLOSED) {
            throw new ServiceDeskException("Chat session is already closed");
        }

        session.setStatus(LiveChatSession.SessionStatus.CLOSED);
        session.setEndedAt(LocalDateTime.now());

        if (request.getRating() != null) {
            session.setRating(request.getRating());
        }
        if (request.getFeedback() != null) {
            session.setFeedback(request.getFeedback());
        }

        session = sessionRepository.save(session);

        // Add system message
        String endedBy = request.getEndedBy() != null ? request.getEndedBy() : "System";
        addSystemMessage(session, "Chat ended by " + endedBy);

        // Notify
        notifyVisitor(session, EventType.CHAT_ENDED);
        notifyAgents(session, EventType.CHAT_ENDED);

        log.info("Chat session {} ended", session.getId());

        return toSessionDto(session);
    }

    // ==================== Message Handling ====================

    @Transactional
    public ChatMessage sendVisitorMessage(SendMessageRequest request) {
        LiveChatSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        // Verify visitor owns the session
        if (!session.getVisitorId().equals(request.getVisitorId())) {
            throw new SecurityException("Visitor does not own this session");
        }

        if (session.getStatus() == LiveChatSession.SessionStatus.CLOSED) {
            throw new ServiceDeskException("Chat session is closed");
        }

        LiveChatMessage message = addMessage(
                session,
                LiveChatMessage.SenderType.VISITOR,
                request.getVisitorId(),
                session.getVisitorName(),
                request.getContent(),
                request.getMessageType() != null ?
                        LiveChatMessage.MessageType.valueOf(request.getMessageType().name()) :
                        LiveChatMessage.MessageType.TEXT
        );

        if (request.getAttachmentUrl() != null) {
            message.setAttachmentUrl(request.getAttachmentUrl());
            message.setAttachmentName(request.getAttachmentName());
            message.setAttachmentSize(request.getAttachmentSize());
            message = messageRepository.save(message);
        }

        ChatMessage dto = toMessageDto(message);

        // Send to WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + session.getId() + "/messages", dto);

        // Notify agent if assigned
        if (session.getAssignedAgentId() != null) {
            messagingTemplate.convertAndSendToUser(
                    session.getAssignedAgentId(),
                    "/queue/chat/messages",
                    dto
            );
        }

        // Clear typing indicator
        stopTyping(request.getSessionId(), request.getVisitorId(), null);

        log.debug("Visitor message sent in session {}", session.getId());

        return dto;
    }

    @Transactional
    public ChatMessage sendAgentMessage(AgentSendMessageRequest request) {
        LiveChatSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        // Verify agent is assigned
        if (!request.getAgentId().equals(session.getAssignedAgentId())) {
            throw new SecurityException("Agent is not assigned to this session");
        }

        if (session.getStatus() == LiveChatSession.SessionStatus.CLOSED) {
            throw new ServiceDeskException("Chat session is closed");
        }

        AgentStatus agentStatus = agentStatuses.get(request.getAgentId());
        String agentName = agentStatus != null ? agentStatus.getAgentName() : "Agent";

        LiveChatMessage message = addMessage(
                session,
                LiveChatMessage.SenderType.AGENT,
                request.getAgentId(),
                agentName,
                request.getContent(),
                request.getMessageType() != null ?
                        LiveChatMessage.MessageType.valueOf(request.getMessageType().name()) :
                        LiveChatMessage.MessageType.TEXT
        );

        if (request.getAttachmentUrl() != null) {
            message.setAttachmentUrl(request.getAttachmentUrl());
            message.setAttachmentName(request.getAttachmentName());
            message = messageRepository.save(message);
        }

        ChatMessage dto = toMessageDto(message);

        // Send to WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + session.getId() + "/messages", dto);

        // Clear typing indicator
        stopTyping(request.getSessionId(), null, request.getAgentId());

        log.debug("Agent message sent in session {}", session.getId());

        return dto;
    }

    // ==================== Typing Indicators ====================

    public void startTyping(String sessionId, String visitorId, String agentId) {
        String userId = visitorId != null ? "visitor:" + visitorId : "agent:" + agentId;
        typingUsers.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        TypingIndicator indicator = TypingIndicator.builder()
                .sessionId(sessionId)
                .visitorId(visitorId)
                .agentId(agentId)
                .isTyping(true)
                .senderType(visitorId != null ? SenderType.VISITOR : SenderType.AGENT)
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/typing", indicator);
    }

    public void stopTyping(String sessionId, String visitorId, String agentId) {
        String userId = visitorId != null ? "visitor:" + visitorId : "agent:" + agentId;
        Set<String> users = typingUsers.get(sessionId);
        if (users != null) {
            users.remove(userId);
        }

        TypingIndicator indicator = TypingIndicator.builder()
                .sessionId(sessionId)
                .visitorId(visitorId)
                .agentId(agentId)
                .isTyping(false)
                .senderType(visitorId != null ? SenderType.VISITOR : SenderType.AGENT)
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/typing", indicator);
    }

    // ==================== Agent Management ====================

    public void updateAgentStatus(AgentStatus status) {
        agentStatuses.put(status.getAgentId(), status);

        // Broadcast to all agents
        messagingTemplate.convertAndSend("/topic/agents/status", status);

        log.debug("Agent status updated: {} -> {}", status.getAgentId(), status.getStatus());
    }

    public List<AgentStatus> getAvailableAgents(String department) {
        return agentStatuses.values().stream()
                .filter(a -> a.getStatus() == AgentAvailability.ONLINE)
                .filter(a -> department == null || a.getDepartments().contains(department))
                .filter(a -> a.getActiveChats() < a.getMaxChats())
                .collect(Collectors.toList());
    }

    public void setAgentOnline(String agentId, String agentName, List<String> departments, int maxChats) {
        AgentStatus status = AgentStatus.builder()
                .agentId(agentId)
                .agentName(agentName)
                .status(AgentAvailability.ONLINE)
                .departments(departments)
                .maxChats(maxChats)
                .activeChats(sessionRepository.countActiveSessionsByAgent(agentId))
                .lastActiveAt(LocalDateTime.now())
                .build();

        updateAgentStatus(status);
    }

    public void setAgentOffline(String agentId) {
        AgentStatus status = agentStatuses.get(agentId);
        if (status != null) {
            status.setStatus(AgentAvailability.OFFLINE);
            updateAgentStatus(status);
        }
    }

    // ==================== Queue Management ====================

    public List<ChatSession> getWaitingQueue(String projectId, String department) {
        List<LiveChatSession> sessions = sessionRepository.findWaitingQueue(department);
        return sessions.stream()
                .filter(s -> projectId == null || s.getProjectId().equals(projectId))
                .map(this::toSessionDto)
                .collect(Collectors.toList());
    }

    public QueueInfo getQueueInfo(String sessionId) {
        LiveChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (session.getStatus() != LiveChatSession.SessionStatus.WAITING) {
            return null;
        }

        int position = sessionRepository.countQueuePositionBefore(session.getCreatedAt(), session.getDepartment()) + 1;
        int estimatedWait = position * 120; // 2 minutes per position (simplified)

        return QueueInfo.builder()
                .sessionId(sessionId)
                .position(position)
                .estimatedWaitTime(estimatedWait)
                .department(session.getDepartment())
                .build();
    }

    // ==================== Queries ====================

    public ChatSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(this::toSessionDto)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
    }

    public List<ChatMessage> getMessages(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    public List<ChatSession> getAgentSessions(String agentId) {
        return sessionRepository.findActiveSessionsByAgent(agentId)
                .stream()
                .map(this::toSessionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(String sessionId, String visitorId, String agentId) {
        LiveChatMessage.SenderType senderType = visitorId != null ?
                LiveChatMessage.SenderType.AGENT : LiveChatMessage.SenderType.VISITOR;

        int updated = messageRepository.markMessagesAsRead(sessionId, senderType, LocalDateTime.now());

        if (updated > 0) {
            ChatEvent event = ChatEvent.builder()
                    .type(EventType.MESSAGE_READ)
                    .sessionId(sessionId)
                    .visitorId(visitorId)
                    .agentId(agentId)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/events", event);
        }
    }

    // ==================== Helper Methods ====================

    private LiveChatMessage addMessage(LiveChatSession session, LiveChatMessage.SenderType senderType,
                                        String senderId, String senderName, String content,
                                        LiveChatMessage.MessageType messageType) {
        LiveChatMessage message = LiveChatMessage.builder()
                .session(session)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .messageType(messageType)
                .isRead(false)
                .build();

        message = messageRepository.save(message);
        session.addMessage(message);
        sessionRepository.save(session);

        return message;
    }

    private void addSystemMessage(LiveChatSession session, String content) {
        addMessage(session, LiveChatMessage.SenderType.SYSTEM, null, "System",
                content, LiveChatMessage.MessageType.SYSTEM);
    }

    private void tryAutoAssign(LiveChatSession session) {
        List<AgentStatus> availableAgents = getAvailableAgents(session.getDepartment());

        if (!availableAgents.isEmpty()) {
            // Find agent with least active chats
            AgentStatus bestAgent = availableAgents.stream()
                    .min(Comparator.comparingInt(AgentStatus::getActiveChats))
                    .orElse(null);

            if (bestAgent != null) {
                AssignAgentRequest request = AssignAgentRequest.builder()
                        .sessionId(session.getId())
                        .agentId(bestAgent.getAgentId())
                        .build();

                try {
                    assignAgent(request);
                } catch (Exception e) {
                    log.warn("Auto-assign failed for session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    private void updateQueuePositions(String department) {
        List<LiveChatSession> waitingSessions = sessionRepository.findWaitingQueue(department);
        int position = 1;
        for (LiveChatSession s : waitingSessions) {
            s.setQueuePosition(position++);
            sessionRepository.save(s);

            // Notify visitor about queue position
            QueueInfo queueInfo = QueueInfo.builder()
                    .sessionId(s.getId())
                    .position(s.getQueuePosition())
                    .estimatedWaitTime(s.getQueuePosition() * 120)
                    .department(department)
                    .build();

            messagingTemplate.convertAndSend("/topic/chat/" + s.getId() + "/queue", queueInfo);
        }
    }

    private void notifyAgents(LiveChatSession session, EventType eventType) {
        ChatEvent event = ChatEvent.builder()
                .type(eventType)
                .sessionId(session.getId())
                .visitorId(session.getVisitorId())
                .agentId(session.getAssignedAgentId())
                .payload(toSessionDto(session))
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/agents/events", event);
    }

    private void notifyVisitor(LiveChatSession session, EventType eventType) {
        ChatEvent event = ChatEvent.builder()
                .type(eventType)
                .sessionId(session.getId())
                .visitorId(session.getVisitorId())
                .agentId(session.getAssignedAgentId())
                .payload(toSessionDto(session))
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + session.getId() + "/events", event);
    }

    private ChatSession toSessionDto(LiveChatSession session) {
        List<ChatMessage> recentMessages = messageRepository
                .findRecentMessages(session.getId(), PageRequest.of(0, 10))
                .stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
        Collections.reverse(recentMessages);

        return ChatSession.builder()
                .id(session.getId())
                .visitorId(session.getVisitorId())
                .visitorName(session.getVisitorName())
                .visitorEmail(session.getVisitorEmail())
                .assignedAgentId(session.getAssignedAgentId())
                .assignedAgentName(session.getAssignedAgentName())
                .status(SessionStatus.valueOf(session.getStatus().name()))
                .department(session.getDepartment())
                .pageUrl(session.getPageUrl())
                .pageTitle(session.getPageTitle())
                .locale(session.getLocale())
                .queuePosition(session.getQueuePosition())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .lastActivityAt(session.getLastActivityAt())
                .messageCount(session.getMessageCount())
                .rating(session.getRating())
                .feedback(session.getFeedback())
                .recentMessages(recentMessages)
                .build();
    }

    private ChatMessage toMessageDto(LiveChatMessage message) {
        return ChatMessage.builder()
                .id(message.getId())
                .sessionId(message.getSession().getId())
                .senderType(SenderType.valueOf(message.getSenderType().name()))
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderAvatar(message.getSenderAvatar())
                .content(message.getContent())
                .messageType(MessageType.valueOf(message.getMessageType().name()))
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentName(message.getAttachmentName())
                .attachmentSize(message.getAttachmentSize())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .timestamp(message.getCreatedAt())
                .build();
    }
}
