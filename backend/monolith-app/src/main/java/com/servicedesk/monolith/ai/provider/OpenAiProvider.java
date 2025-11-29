package com.servicedesk.monolith.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.monolith.ai.config.AiProperties;
import com.servicedesk.monolith.ai.dto.ChatRequest;
import com.servicedesk.monolith.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAiProvider implements AiProvider {

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public ChatResponse chat(ChatRequest request, String systemPrompt) {
        try {
            String url = properties.getOpenai().getBaseUrl() + "/chat/completions";

            List<Map<String, String>> messages = new ArrayList<>();

            // Add system prompt
            messages.add(Map.of(
                    "role", "system",
                    "content", systemPrompt
            ));

            // Add history
            if (request.getHistory() != null) {
                for (ChatRequest.Message msg : request.getHistory()) {
                    messages.add(Map.of(
                            "role", msg.getRole(),
                            "content", msg.getContent()
                    ));
                }
            }

            // Add current message
            messages.add(Map.of(
                    "role", "user",
                    "content", request.getMessage()
            ));

            Map<String, Object> body = new HashMap<>();
            body.put("model", properties.getOpenai().getModel());
            body.put("messages", messages);
            body.put("temperature", properties.getOpenai().getTemperature());
            body.put("max_tokens", properties.getOpenai().getMaxTokens());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getOpenai().getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            JsonNode choice = responseBody.get("choices").get(0);
            String content = choice.get("message").get("content").asText();
            int tokensUsed = responseBody.get("usage").get("total_tokens").asInt();

            return ChatResponse.builder()
                    .message(content)
                    .provider("openai")
                    .tokensUsed(tokensUsed)
                    .conversationId(request.getConversationId())
                    .build();

        } catch (Exception e) {
            log.error("OpenAI chat error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get response from OpenAI: " + e.getMessage());
        }
    }

    @Override
    public String complete(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .message(prompt)
                .build();
        ChatResponse response = chat(request, "You are a helpful assistant.");
        return response.getMessage();
    }

    @Override
    public float[] embed(String text) {
        try {
            String url = properties.getOpenai().getBaseUrl() + "/embeddings";

            Map<String, Object> body = new HashMap<>();
            body.put("model", properties.getOpenai().getEmbeddingModel());
            body.put("input", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getOpenai().getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            JsonNode embeddingNode = responseBody.get("data").get(0).get("embedding");

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }

            return embedding;

        } catch (Exception e) {
            log.error("OpenAI embedding error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get embedding from OpenAI: " + e.getMessage());
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        try {
            String url = properties.getOpenai().getBaseUrl() + "/embeddings";

            Map<String, Object> body = new HashMap<>();
            body.put("model", properties.getOpenai().getEmbeddingModel());
            body.put("input", texts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getOpenai().getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            JsonNode dataNode = responseBody.get("data");

            List<float[]> embeddings = new ArrayList<>();
            for (JsonNode item : dataNode) {
                JsonNode embeddingNode = item.get("embedding");
                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    embedding[i] = (float) embeddingNode.get(i).asDouble();
                }
                embeddings.add(embedding);
            }

            return embeddings;

        } catch (Exception e) {
            log.error("OpenAI batch embedding error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get embeddings from OpenAI: " + e.getMessage());
        }
    }

    @Override
    public String summarize(String text, int maxLength) {
        String prompt = String.format(
                "Summarize the following text in no more than %d characters:\n\n%s",
                maxLength, text
        );
        return complete(prompt);
    }

    @Override
    public String translate(String text, String targetLanguage) {
        String prompt = String.format(
                "Translate the following text to %s:\n\n%s",
                targetLanguage, text
        );
        return complete(prompt);
    }
}
