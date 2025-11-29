package com.servicedesk.monolith.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    private String message;
    private String ticketId;
    private String userId;
    private String conversationId;
    private List<Message> history;
    private String provider;
    private Boolean useRag = true;
    private String locale = "en";
    private String projectId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
