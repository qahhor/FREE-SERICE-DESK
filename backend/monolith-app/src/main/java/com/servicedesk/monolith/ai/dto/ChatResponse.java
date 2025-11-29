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
public class ChatResponse {
    private String message;
    private String conversationId;
    private List<Source> sources;
    private Double confidence;
    private String provider;
    private Integer tokensUsed;
    private Boolean requiresHumanReview;
    private String suggestedCategory;
    private String suggestedPriority;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Source {
        private String articleId;
        private String title;
        private String url;
        private Double score;
    }
}
