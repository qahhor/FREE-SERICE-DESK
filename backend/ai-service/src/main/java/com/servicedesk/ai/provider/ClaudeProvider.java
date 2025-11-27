package com.servicedesk.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.ai.config.AiProperties;
import com.servicedesk.ai.dto.ChatRequest;
import com.servicedesk.ai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClaudeProvider implements AiProvider {

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OpenAiProvider openAiProvider; // Use OpenAI for embeddings

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public ChatResponse chat(ChatRequest request, String systemPrompt) {
        try {
            String url = properties.getClaude().getBaseUrl() + "/messages";

            List<Map<String, Object>> messages = new ArrayList<>();

            // Add history
            if (request.getHistory() != null) {
                for (ChatRequest.Message msg : request.getHistory()) {
                    messages.add(Map.of(
                            "role", msg.getRole().equals("assistant") ? "assistant" : "user",
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
            body.put("model", properties.getClaude().getModel());
            body.put("max_tokens", properties.getClaude().getMaxTokens());
            body.put("system", systemPrompt);
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", properties.getClaude().getApiKey());
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            JsonNode content = responseBody.get("content").get(0);
            String text = content.get("text").asText();

            int inputTokens = responseBody.get("usage").get("input_tokens").asInt();
            int outputTokens = responseBody.get("usage").get("output_tokens").asInt();

            return ChatResponse.builder()
                    .message(text)
                    .provider("claude")
                    .tokensUsed(inputTokens + outputTokens)
                    .conversationId(request.getConversationId())
                    .build();

        } catch (Exception e) {
            log.error("Claude chat error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get response from Claude: " + e.getMessage());
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
        // Claude doesn't have native embeddings, use OpenAI
        return openAiProvider.embed(text);
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        // Claude doesn't have native embeddings, use OpenAI
        return openAiProvider.embedBatch(texts);
    }

    @Override
    public String summarize(String text, int maxLength) {
        String prompt = String.format(
                "Please summarize the following text concisely in no more than %d characters:\n\n%s",
                maxLength, text
        );
        return complete(prompt);
    }

    @Override
    public String translate(String text, String targetLanguage) {
        String prompt = String.format(
                "Translate the following text to %s. Provide only the translation, no explanations:\n\n%s",
                targetLanguage, text
        );
        return complete(prompt);
    }
}
