package com.servicedesk.monolith.channel.controller;

import com.servicedesk.monolith.channel.dto.WidgetConversationDto;
import com.servicedesk.monolith.channel.dto.WidgetMessageDto;
import com.servicedesk.monolith.channel.entity.WidgetMessage;
import com.servicedesk.monolith.channel.service.WidgetService;
import com.servicedesk.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/widget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Widget needs CORS
public class WidgetController {

    private final WidgetService widgetService;

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<WidgetConversationDto>> createConversation(
            @RequestBody CreateConversationRequest request,
            HttpServletRequest httpRequest) {

        WidgetService.CreateConversationRequest serviceRequest = WidgetService.CreateConversationRequest.builder()
                .projectId(request.getProjectId())
                .visitorId(request.getVisitorId())
                .visitorName(request.getVisitorName())
                .visitorEmail(request.getVisitorEmail())
                .visitorPhone(request.getVisitorPhone())
                .pageUrl(request.getPageUrl())
                .pageTitle(request.getPageTitle())
                .userAgent(httpRequest.getHeader("User-Agent"))
                .ipAddress(getClientIp(httpRequest))
                .locale(request.getLocale())
                .build();

        WidgetConversationDto conversation = widgetService.createConversation(serviceRequest);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<WidgetConversationDto>> getConversation(
            @PathVariable String conversationId,
            @RequestParam String visitorId) {
        return ResponseEntity.ok(ApiResponse.success(
                widgetService.getConversation(conversationId, visitorId)));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<WidgetMessageDto>>> getMessages(
            @PathVariable String conversationId,
            @RequestParam String visitorId) {
        return ResponseEntity.ok(ApiResponse.success(
                widgetService.getMessages(conversationId, visitorId)));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<WidgetMessageDto>> sendMessage(
            @PathVariable String conversationId,
            @RequestBody SendMessageRequest request) {
        WidgetMessageDto message = widgetService.addMessage(
                conversationId,
                request.getVisitorId(),
                request.getContent(),
                WidgetMessage.SenderType.VISITOR,
                request.getVisitorId(),
                null
        );
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/conversations/{conversationId}/typing")
    public ResponseEntity<ApiResponse<Void>> sendTypingIndicator(
            @PathVariable String conversationId,
            @RequestBody TypingRequest request) {
        widgetService.sendTypingIndicator(conversationId, request.getVisitorId(), request.isTyping());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/conversations/{conversationId}/close")
    public ResponseEntity<ApiResponse<Void>> closeConversation(
            @PathVariable String conversationId,
            @RequestBody CloseConversationRequest request) {
        widgetService.closeConversation(conversationId, request.getRating(), request.getFeedback());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Agent endpoints

    @GetMapping("/agent/conversations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<WidgetConversationDto>>> getActiveConversations(
            @RequestParam String projectId) {
        return ResponseEntity.ok(ApiResponse.success(
                widgetService.getActiveConversations(projectId)));
    }

    @GetMapping("/agent/my-conversations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<WidgetConversationDto>>> getMyConversations(
            @RequestParam String agentId) {
        return ResponseEntity.ok(ApiResponse.success(
                widgetService.getAgentConversations(agentId)));
    }

    @PostMapping("/agent/conversations/{conversationId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<Void>> assignConversation(
            @PathVariable String conversationId,
            @RequestParam String agentId) {
        widgetService.assignToAgent(conversationId, agentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/agent/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<WidgetMessageDto>> sendAgentMessage(
            @PathVariable String conversationId,
            @RequestBody AgentMessageRequest request) {
        WidgetMessageDto message = widgetService.addMessage(
                conversationId,
                null,
                request.getContent(),
                WidgetMessage.SenderType.AGENT,
                request.getAgentId(),
                request.getAgentName()
        );
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Data
    public static class CreateConversationRequest {
        private String projectId;
        private String visitorId;
        private String visitorName;
        private String visitorEmail;
        private String visitorPhone;
        private String pageUrl;
        private String pageTitle;
        private String locale;
    }

    @Data
    public static class SendMessageRequest {
        private String visitorId;
        private String content;
    }

    @Data
    public static class TypingRequest {
        private String visitorId;
        private boolean typing;
    }

    @Data
    public static class CloseConversationRequest {
        private Integer rating;
        private String feedback;
    }

    @Data
    public static class AgentMessageRequest {
        private String agentId;
        private String agentName;
        private String content;
    }
}
