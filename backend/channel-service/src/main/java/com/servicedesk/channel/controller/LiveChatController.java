package com.servicedesk.channel.controller;

import com.servicedesk.channel.dto.LiveChatDto;
import com.servicedesk.channel.dto.LiveChatDto.*;
import com.servicedesk.channel.service.LiveChatService;
import com.servicedesk.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/livechat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Live Chat", description = "Real-time live chat endpoints")
public class LiveChatController {

    private final LiveChatService liveChatService;

    // ==================== Visitor Endpoints ====================

    @PostMapping("/sessions/start")
    @Operation(summary = "Start a new chat session", description = "Initiates a new live chat session for a visitor")
    public ResponseEntity<ApiResponse<ChatSession>> startChat(
            @Valid @RequestBody StartChatRequest request) {

        ChatSession session = liveChatService.startChat(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Chat session started"));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get chat session", description = "Retrieve chat session details")
    public ResponseEntity<ApiResponse<ChatSession>> getSession(@PathVariable String sessionId) {
        ChatSession session = liveChatService.getSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get chat messages", description = "Retrieve all messages in a chat session")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(@PathVariable String sessionId) {
        List<ChatMessage> messages = liveChatService.getMessages(sessionId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/sessions/{sessionId}/messages/visitor")
    @Operation(summary = "Send visitor message", description = "Send a message from visitor to agent")
    public ResponseEntity<ApiResponse<ChatMessage>> sendVisitorMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody SendMessageRequest request) {

        request.setSessionId(sessionId);
        ChatMessage message = liveChatService.sendVisitorMessage(request);
        return ResponseEntity.ok(ApiResponse.success(message, "Message sent"));
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End chat session", description = "End a chat session with optional rating")
    public ResponseEntity<ApiResponse<ChatSession>> endChat(
            @PathVariable String sessionId,
            @Valid @RequestBody EndChatRequest request) {

        request.setSessionId(sessionId);
        ChatSession session = liveChatService.endChat(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Chat ended"));
    }

    @GetMapping("/sessions/{sessionId}/queue")
    @Operation(summary = "Get queue info", description = "Get visitor's position in the queue")
    public ResponseEntity<ApiResponse<QueueInfo>> getQueueInfo(@PathVariable String sessionId) {
        QueueInfo queueInfo = liveChatService.getQueueInfo(sessionId);
        return ResponseEntity.ok(ApiResponse.success(queueInfo));
    }

    @PostMapping("/sessions/{sessionId}/read")
    @Operation(summary = "Mark messages as read", description = "Mark all messages as read for visitor")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable String sessionId,
            @RequestParam String visitorId) {

        liveChatService.markMessagesAsRead(sessionId, visitorId, null);
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    // ==================== Agent Endpoints ====================

    @PostMapping("/sessions/{sessionId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Assign agent to chat", description = "Assign an agent to handle a waiting chat")
    public ResponseEntity<ApiResponse<ChatSession>> assignAgent(
            @PathVariable String sessionId,
            @Valid @RequestBody AssignAgentRequest request) {

        request.setSessionId(sessionId);
        ChatSession session = liveChatService.assignAgent(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Agent assigned"));
    }

    @PostMapping("/sessions/{sessionId}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Transfer chat", description = "Transfer chat to another agent or department")
    public ResponseEntity<ApiResponse<ChatSession>> transferChat(
            @PathVariable String sessionId,
            @Valid @RequestBody TransferChatRequest request) {

        request.setSessionId(sessionId);
        ChatSession session = liveChatService.transferChat(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Chat transferred"));
    }

    @PostMapping("/sessions/{sessionId}/messages/agent")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Send agent message", description = "Send a message from agent to visitor")
    public ResponseEntity<ApiResponse<ChatMessage>> sendAgentMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody AgentSendMessageRequest request) {

        request.setSessionId(sessionId);
        ChatMessage message = liveChatService.sendAgentMessage(request);
        return ResponseEntity.ok(ApiResponse.success(message, "Message sent"));
    }

    @PostMapping("/sessions/{sessionId}/read/agent")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Mark messages as read (agent)", description = "Mark all visitor messages as read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsReadAgent(
            @PathVariable String sessionId,
            @RequestParam String agentId) {

        liveChatService.markMessagesAsRead(sessionId, null, agentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get waiting queue", description = "Get all waiting chat sessions")
    public ResponseEntity<ApiResponse<List<ChatSession>>> getWaitingQueue(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String department) {

        List<ChatSession> queue = liveChatService.getWaitingQueue(projectId, department);
        return ResponseEntity.ok(ApiResponse.success(queue));
    }

    @GetMapping("/agents/{agentId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get agent's active chats", description = "Get all active chat sessions for an agent")
    public ResponseEntity<ApiResponse<List<ChatSession>>> getAgentSessions(@PathVariable String agentId) {
        List<ChatSession> sessions = liveChatService.getAgentSessions(agentId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/agents/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Get available agents", description = "Get list of available agents")
    public ResponseEntity<ApiResponse<List<AgentStatus>>> getAvailableAgents(
            @RequestParam(required = false) String department) {

        List<AgentStatus> agents = liveChatService.getAvailableAgents(department);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @PostMapping("/agents/{agentId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Update agent status", description = "Update agent's availability status")
    public ResponseEntity<ApiResponse<Void>> updateAgentStatus(
            @PathVariable String agentId,
            @Valid @RequestBody AgentStatus status) {

        status.setAgentId(agentId);
        liveChatService.updateAgentStatus(status);
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated"));
    }

    @PostMapping("/agents/{agentId}/online")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Set agent online", description = "Mark agent as online and available for chats")
    public ResponseEntity<ApiResponse<Void>> setAgentOnline(
            @PathVariable String agentId,
            @RequestParam String agentName,
            @RequestParam(required = false) List<String> departments,
            @RequestParam(defaultValue = "5") int maxChats) {

        liveChatService.setAgentOnline(agentId, agentName, departments, maxChats);
        return ResponseEntity.ok(ApiResponse.success(null, "Agent is now online"));
    }

    @PostMapping("/agents/{agentId}/offline")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Set agent offline", description = "Mark agent as offline")
    public ResponseEntity<ApiResponse<Void>> setAgentOffline(@PathVariable String agentId) {
        liveChatService.setAgentOffline(agentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Agent is now offline"));
    }

    // ==================== WebSocket Message Handlers ====================

    @MessageMapping("/chat/{sessionId}/message")
    @SendTo("/topic/chat/{sessionId}/messages")
    public ChatMessage handleChatMessage(
            @DestinationVariable String sessionId,
            @Payload SendMessageRequest request) {

        request.setSessionId(sessionId);
        return liveChatService.sendVisitorMessage(request);
    }

    @MessageMapping("/chat/{sessionId}/agent/message")
    @SendTo("/topic/chat/{sessionId}/messages")
    public ChatMessage handleAgentMessage(
            @DestinationVariable String sessionId,
            @Payload AgentSendMessageRequest request) {

        request.setSessionId(sessionId);
        return liveChatService.sendAgentMessage(request);
    }

    @MessageMapping("/chat/{sessionId}/typing")
    @SendTo("/topic/chat/{sessionId}/typing")
    public TypingIndicator handleTyping(
            @DestinationVariable String sessionId,
            @Payload TypingIndicator indicator) {

        indicator.setSessionId(sessionId);

        if (indicator.getIsTyping()) {
            liveChatService.startTyping(sessionId, indicator.getVisitorId(), indicator.getAgentId());
        } else {
            liveChatService.stopTyping(sessionId, indicator.getVisitorId(), indicator.getAgentId());
        }

        return indicator;
    }

    @MessageMapping("/chat/{sessionId}/read")
    public void handleRead(
            @DestinationVariable String sessionId,
            @Payload ChatEvent event) {

        liveChatService.markMessagesAsRead(sessionId, event.getVisitorId(), event.getAgentId());
    }
}
