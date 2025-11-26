package com.servicedesk.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "servicedesk.ai")
@Data
public class AiProperties {

    private OpenAiConfig openai = new OpenAiConfig();
    private ClaudeConfig claude = new ClaudeConfig();
    private RagConfig rag = new RagConfig();
    private String defaultProvider = "openai";

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String model = "gpt-4-turbo-preview";
        private String embeddingModel = "text-embedding-3-small";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        private String baseUrl = "https://api.openai.com/v1";
    }

    @Data
    public static class ClaudeConfig {
        private String apiKey;
        private String model = "claude-3-sonnet-20240229";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        private String baseUrl = "https://api.anthropic.com/v1";
    }

    @Data
    public static class RagConfig {
        private Boolean enabled = true;
        private Integer topK = 5;
        private Double similarityThreshold = 0.7;
        private String vectorStore = "elasticsearch";
    }
}
