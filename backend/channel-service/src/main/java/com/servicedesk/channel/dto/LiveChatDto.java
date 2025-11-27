package com.servicedesk.channel.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class LiveChatDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatSession {
        private String id;
        private String visitorId;
        private String visitorName;
        private String visitorEmail;
        private String assignedAgentId;
        private String assignedAgentName;
        private SessionStatus status;
        private String department;
        private String pageUrl;
        private String pageTitle;
        private String locale;
        private Integer queuePosition;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private LocalDateTime lastActivityAt;
        private Integer messageCount;
        private Integer rating;
        private String feedback;
        private Map<String, Object> visitorInfo;
        private List<ChatMessage> recentMessages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage {
        private String id;
        private String sessionId;
        private SenderType senderType;
        private String senderId;
        private String senderName;
        private String senderAvatar;
        private String content;
        private MessageType messageType;
        private String attachmentUrl;
        private String attachmentName;
        private Long attachmentSize;
        private Boolean isRead;
        private LocalDateTime readAt;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartChatRequest {
        private String projectId;
        private String visitorId;
        private String visitorName;
        private String visitorEmail;
        private String visitorPhone;
        private String department;
        private String pageUrl;
        private String pageTitle;
        private String userAgent;
        private String ipAddress;
        private String locale;
        private String initialMessage;
        private Map<String, Object> customFields;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private String sessionId;
        private String visitorId;
        private String content;
        private MessageType messageType;
        private String attachmentUrl;
        private String attachmentName;
        private Long attachmentSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentSendMessageRequest {
        private String sessionId;
        private String agentId;
        private String content;
        private MessageType messageType;
        private String attachmentUrl;
        private String attachmentName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypingIndicator {
        private String sessionId;
        private String visitorId;
        private String agentId;
        private Boolean isTyping;
        private SenderType senderType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignAgentRequest {
        private String sessionId;
        private String agentId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransferChatRequest {
        private String sessionId;
        private String fromAgentId;
        private String toAgentId;
        private String toDepartment;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EndChatRequest {
        private String sessionId;
        private String endedBy;
        private Integer rating;
        private String feedback;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatEvent {
        private EventType type;
        private String sessionId;
        private String visitorId;
        private String agentId;
        private Object payload;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentStatus {
        private String agentId;
        private String agentName;
        private String avatar;
        private AgentAvailability status;
        private Integer activeChats;
        private Integer maxChats;
        private List<String> departments;
        private LocalDateTime lastActiveAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueInfo {
        private String sessionId;
        private Integer position;
        private Integer estimatedWaitTime;
        private String department;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CannedResponse {
        private String id;
        private String title;
        private String content;
        private String category;
        private List<String> tags;
        private String shortcut;
    }

    public enum SessionStatus {
        WAITING,        // Waiting in queue
        ACTIVE,         // Chat is active with agent
        ON_HOLD,        // Put on hold by agent
        TRANSFERRED,    // Being transferred
        CLOSED,         // Chat ended normally
        ABANDONED,      // Visitor left without agent response
        MISSED          // No agent was available
    }

    public enum SenderType {
        VISITOR,
        AGENT,
        SYSTEM,
        BOT
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        AUDIO,
        VIDEO,
        CARD,
        BUTTONS,
        CAROUSEL,
        QUICK_REPLIES,
        FORM,
        SYSTEM
    }

    public enum EventType {
        CHAT_STARTED,
        CHAT_ASSIGNED,
        CHAT_TRANSFERRED,
        CHAT_ENDED,
        MESSAGE_RECEIVED,
        MESSAGE_SENT,
        MESSAGE_READ,
        TYPING_STARTED,
        TYPING_STOPPED,
        AGENT_JOINED,
        AGENT_LEFT,
        VISITOR_CONNECTED,
        VISITOR_DISCONNECTED,
        QUEUE_POSITION_UPDATED
    }

    public enum AgentAvailability {
        ONLINE,
        AWAY,
        BUSY,
        OFFLINE
    }
}
