package com.servicedesk.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAnalysis {
    private String ticketId;
    private String suggestedCategory;
    private String suggestedPriority;
    private String suggestedTeam;
    private Double sentiment;
    private String sentimentLabel;
    private List<String> detectedTopics;
    private List<String> suggestedTags;
    private Boolean isUrgent;
    private String language;
    private String summary;
    private List<SuggestedResponse> suggestedResponses;
    private List<RelatedArticle> relatedArticles;
    private Map<String, Double> categoryConfidences;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestedResponse {
        private String text;
        private Double confidence;
        private String tone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedArticle {
        private String id;
        private String title;
        private String url;
        private Double relevance;
    }
}
