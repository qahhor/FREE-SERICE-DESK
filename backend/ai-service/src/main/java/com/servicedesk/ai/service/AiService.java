package com.servicedesk.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.ai.config.AiProperties;
import com.servicedesk.ai.dto.ChatRequest;
import com.servicedesk.ai.dto.ChatResponse;
import com.servicedesk.ai.dto.TicketAnalysis;
import com.servicedesk.ai.provider.AiProvider;
import com.servicedesk.ai.provider.ClaudeProvider;
import com.servicedesk.ai.provider.OpenAiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final AiProperties properties;
    private final OpenAiProvider openAiProvider;
    private final ClaudeProvider claudeProvider;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are a helpful customer support assistant for a Service Desk platform.
            Your role is to help users with their questions and issues.

            Guidelines:
            - Be professional, friendly, and concise
            - If you're not sure about something, say so
            - When relevant context is provided, use it to answer questions
            - Format your responses in a clear, readable way
            - If the issue requires human intervention, acknowledge this

            Relevant context from knowledge base:
            %s
            """;

    private static final String TICKET_ANALYSIS_PROMPT = """
            Analyze the following support ticket and provide:
            1. Suggested category (choose from: Technical Support, Billing, Account, General Inquiry, Feature Request, Bug Report)
            2. Suggested priority (choose from: LOW, MEDIUM, HIGH, URGENT)
            3. Sentiment analysis (score from -1 to 1, where -1 is very negative, 0 is neutral, 1 is very positive)
            4. Key topics detected
            5. Suggested tags
            6. Whether it's urgent (boolean)
            7. Brief summary (max 100 chars)
            8. 2-3 suggested response templates

            Respond in JSON format with the following structure:
            {
                "suggestedCategory": "string",
                "suggestedPriority": "string",
                "sentiment": number,
                "detectedTopics": ["string"],
                "suggestedTags": ["string"],
                "isUrgent": boolean,
                "summary": "string",
                "suggestedResponses": [{"text": "string", "tone": "string"}]
            }

            Ticket Subject: %s
            Ticket Description: %s
            """;

    public ChatResponse chat(ChatRequest request) {
        AiProvider provider = getProvider(request.getProvider());

        String context = "";
        List<ChatResponse.Source> sources = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getUseRag()) && properties.getRag().getEnabled()) {
            var ragResult = searchKnowledgeBase(request.getMessage(), request.getLocale(), request.getProjectId());
            context = ragResult.context();
            sources = ragResult.sources();
        }

        String systemPrompt = String.format(SYSTEM_PROMPT, context);

        ChatResponse response = provider.chat(request, systemPrompt);
        response.setSources(sources);

        // Check if human review is needed
        response.setRequiresHumanReview(shouldRequireHumanReview(response.getMessage(), sources));

        return response;
    }

    public TicketAnalysis analyzeTicket(String ticketId, String subject, String description) {
        AiProvider provider = getProvider(null);

        String prompt = String.format(TICKET_ANALYSIS_PROMPT, subject, description);

        try {
            String response = provider.complete(prompt);

            // Parse JSON response
            response = extractJson(response);
            TicketAnalysis analysis = objectMapper.readValue(response, TicketAnalysis.class);
            analysis.setTicketId(ticketId);

            // Set sentiment label
            if (analysis.getSentiment() != null) {
                if (analysis.getSentiment() < -0.3) {
                    analysis.setSentimentLabel("negative");
                } else if (analysis.getSentiment() > 0.3) {
                    analysis.setSentimentLabel("positive");
                } else {
                    analysis.setSentimentLabel("neutral");
                }
            }

            // Search for related articles
            var ragResult = searchKnowledgeBase(subject + " " + description, "en", null);
            List<TicketAnalysis.RelatedArticle> relatedArticles = ragResult.sources().stream()
                    .map(s -> TicketAnalysis.RelatedArticle.builder()
                            .id(s.getArticleId())
                            .title(s.getTitle())
                            .url(s.getUrl())
                            .relevance(s.getScore())
                            .build())
                    .collect(Collectors.toList());
            analysis.setRelatedArticles(relatedArticles);

            return analysis;

        } catch (Exception e) {
            log.error("Failed to analyze ticket: {}", e.getMessage(), e);

            // Return default analysis on error
            return TicketAnalysis.builder()
                    .ticketId(ticketId)
                    .suggestedCategory("General Inquiry")
                    .suggestedPriority("MEDIUM")
                    .sentiment(0.0)
                    .sentimentLabel("neutral")
                    .isUrgent(false)
                    .detectedTopics(List.of())
                    .suggestedTags(List.of())
                    .build();
        }
    }

    public String generateResponse(String ticketSubject, String ticketDescription, String tone) {
        AiProvider provider = getProvider(null);

        String prompt = String.format("""
                Generate a professional customer support response for the following ticket.
                Tone: %s

                Ticket Subject: %s
                Ticket Description: %s

                Provide only the response text, no explanations.
                """, tone, ticketSubject, ticketDescription);

        return provider.complete(prompt);
    }

    public String summarize(String text, int maxLength) {
        AiProvider provider = getProvider(null);
        return provider.summarize(text, maxLength);
    }

    public String translate(String text, String targetLanguage) {
        AiProvider provider = getProvider(null);
        return provider.translate(text, targetLanguage);
    }

    public float[] embed(String text) {
        return openAiProvider.embed(text);
    }

    public List<float[]> embedBatch(List<String> texts) {
        return openAiProvider.embedBatch(texts);
    }

    private AiProvider getProvider(String providerName) {
        String name = providerName != null ? providerName : properties.getDefaultProvider();
        return "claude".equalsIgnoreCase(name) ? claudeProvider : openAiProvider;
    }

    private RagResult searchKnowledgeBase(String query, String locale, String projectId) {
        try {
            // Generate embedding for the query
            float[] queryEmbedding = openAiProvider.embed(query);

            // Search using vector similarity
            // Note: This is a simplified example. In production, use proper kNN query
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> {
                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(query)
                                                .fields("title^3", "summary^2", "content")
                                                .fuzziness("AUTO")
                                        )
                                );

                                b.filter(f -> f.term(t -> t.field("status").value("PUBLISHED")));

                                if (locale != null) {
                                    b.filter(f -> f.term(t -> t.field("locale").value(locale)));
                                }

                                if (projectId != null) {
                                    b.filter(f -> f.term(t -> t.field("projectId").value(projectId)));
                                }

                                return b;
                            })
                    )
                    .build();

            SearchHits<Map> hits = elasticsearchOperations.search(searchQuery, Map.class,
                    org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.of("articles"));

            StringBuilder context = new StringBuilder();
            List<ChatResponse.Source> sources = new ArrayList<>();

            int count = 0;
            for (SearchHit<Map> hit : hits) {
                if (count >= properties.getRag().getTopK()) break;
                if (hit.getScore() < properties.getRag().getSimilarityThreshold()) continue;

                Map<String, Object> article = hit.getContent();
                String title = (String) article.get("title");
                String content = (String) article.get("content");
                String articleId = (String) article.get("id");

                context.append("Article: ").append(title).append("\n");
                context.append(content != null ? content.substring(0, Math.min(content.length(), 500)) : "");
                context.append("\n\n");

                sources.add(ChatResponse.Source.builder()
                        .articleId(articleId)
                        .title(title)
                        .url("/knowledge/articles/" + articleId)
                        .score((double) hit.getScore())
                        .build());

                count++;
            }

            return new RagResult(context.toString(), sources);

        } catch (Exception e) {
            log.error("Knowledge base search error: {}", e.getMessage(), e);
            return new RagResult("", List.of());
        }
    }

    private boolean shouldRequireHumanReview(String response, List<ChatResponse.Source> sources) {
        // Require human review if:
        // 1. No relevant sources found
        // 2. Response contains uncertainty indicators
        if (sources.isEmpty()) {
            return true;
        }

        String lowerResponse = response.toLowerCase();
        return lowerResponse.contains("i'm not sure") ||
                lowerResponse.contains("i don't know") ||
                lowerResponse.contains("you should contact") ||
                lowerResponse.contains("please wait for");
    }

    private String extractJson(String response) {
        // Extract JSON from response if wrapped in markdown code blocks
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            return response.substring(start, end).trim();
        } else if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            return response.substring(start, end).trim();
        }
        return response.trim();
    }

    private record RagResult(String context, List<ChatResponse.Source> sources) {}
}
