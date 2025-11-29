package com.servicedesk.monolith.ai.provider;

import com.servicedesk.monolith.ai.dto.ChatRequest;
import com.servicedesk.monolith.ai.dto.ChatResponse;

import java.util.List;

public interface AiProvider {

    String getName();

    ChatResponse chat(ChatRequest request, String systemPrompt);

    String complete(String prompt);

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);

    String summarize(String text, int maxLength);

    String translate(String text, String targetLanguage);
}
