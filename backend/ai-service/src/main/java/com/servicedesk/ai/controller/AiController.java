package com.servicedesk.ai.controller;

import com.servicedesk.ai.dto.ChatRequest;
import com.servicedesk.ai.dto.ChatResponse;
import com.servicedesk.ai.dto.TicketAnalysis;
import com.servicedesk.ai.service.AiService;
import com.servicedesk.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.chat(request)));
    }

    @PostMapping("/analyze-ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<TicketAnalysis>> analyzeTicket(
            @RequestBody AnalyzeTicketRequest request) {
        TicketAnalysis analysis = aiService.analyzeTicket(
                request.getTicketId(),
                request.getSubject(),
                request.getDescription()
        );
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    @PostMapping("/generate-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<GenerateResponseResult>> generateResponse(
            @RequestBody GenerateResponseRequest request) {
        String response = aiService.generateResponse(
                request.getSubject(),
                request.getDescription(),
                request.getTone() != null ? request.getTone() : "professional"
        );
        return ResponseEntity.ok(ApiResponse.success(new GenerateResponseResult(response)));
    }

    @PostMapping("/summarize")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<SummarizeResult>> summarize(
            @RequestBody SummarizeRequest request) {
        String summary = aiService.summarize(
                request.getText(),
                request.getMaxLength() != null ? request.getMaxLength() : 200
        );
        return ResponseEntity.ok(ApiResponse.success(new SummarizeResult(summary)));
    }

    @PostMapping("/translate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<TranslateResult>> translate(
            @RequestBody TranslateRequest request) {
        String translation = aiService.translate(
                request.getText(),
                request.getTargetLanguage()
        );
        return ResponseEntity.ok(ApiResponse.success(new TranslateResult(translation)));
    }

    @PostMapping("/embed")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<EmbedResult>> embed(@RequestBody EmbedRequest request) {
        float[] embedding = aiService.embed(request.getText());
        return ResponseEntity.ok(ApiResponse.success(new EmbedResult(embedding)));
    }

    // Request/Response DTOs

    @Data
    public static class AnalyzeTicketRequest {
        private String ticketId;
        private String subject;
        private String description;
    }

    @Data
    public static class GenerateResponseRequest {
        private String subject;
        private String description;
        private String tone;
    }

    public record GenerateResponseResult(String response) {}

    @Data
    public static class SummarizeRequest {
        private String text;
        private Integer maxLength;
    }

    public record SummarizeResult(String summary) {}

    @Data
    public static class TranslateRequest {
        private String text;
        private String targetLanguage;
    }

    public record TranslateResult(String translation) {}

    @Data
    public static class EmbedRequest {
        private String text;
    }

    public record EmbedResult(float[] embedding) {}
}
