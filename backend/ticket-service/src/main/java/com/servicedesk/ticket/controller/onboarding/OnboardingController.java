package com.servicedesk.ticket.controller.onboarding;

import com.servicedesk.ticket.dto.onboarding.OnboardingProgressDto;
import com.servicedesk.ticket.dto.onboarding.OnboardingStepDto;
import com.servicedesk.ticket.entity.onboarding.UserOnboarding;
import com.servicedesk.ticket.service.onboarding.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "User onboarding management")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/progress")
    @Operation(summary = "Get current user's onboarding progress")
    public ResponseEntity<OnboardingProgressDto> getProgress(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "AGENT") UserOnboarding.UserRole role) {

        OnboardingProgressDto progress = onboardingService.getOrCreateProgress(userId, tenantId, role);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/progress/{userId}")
    @Operation(summary = "Get specific user's onboarding progress (admin)")
    public ResponseEntity<OnboardingProgressDto> getUserProgress(@PathVariable UUID userId) {
        return onboardingService.getProgress(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/steps/{stepId}/complete")
    @Operation(summary = "Mark a step as completed")
    public ResponseEntity<OnboardingProgressDto> completeStep(
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable String stepId) {

        OnboardingProgressDto progress = onboardingService.completeStep(userId, stepId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/tours/{tourId}/complete")
    @Operation(summary = "Mark a tour as completed")
    public ResponseEntity<OnboardingProgressDto> completeTour(
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable String tourId) {

        OnboardingProgressDto progress = onboardingService.completeTour(userId, tourId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/welcome/dismiss")
    @Operation(summary = "Dismiss welcome wizard")
    public ResponseEntity<OnboardingProgressDto> dismissWelcome(
            @RequestHeader("X-User-ID") UUID userId) {

        OnboardingProgressDto progress = onboardingService.dismissWelcome(userId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/skip")
    @Operation(summary = "Skip onboarding entirely")
    public ResponseEntity<OnboardingProgressDto> skipOnboarding(
            @RequestHeader("X-User-ID") UUID userId) {

        OnboardingProgressDto progress = onboardingService.skipOnboarding(userId);
        return ResponseEntity.ok(progress);
    }

    @PutMapping("/hints")
    @Operation(summary = "Toggle contextual hints")
    public ResponseEntity<OnboardingProgressDto> toggleHints(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam boolean enabled) {

        OnboardingProgressDto progress = onboardingService.toggleHints(userId, enabled);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/reset/{userId}")
    @Operation(summary = "Reset onboarding for a user (admin)")
    public ResponseEntity<OnboardingProgressDto> resetOnboarding(@PathVariable UUID userId) {
        OnboardingProgressDto progress = onboardingService.resetOnboarding(userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/steps")
    @Operation(summary = "Get all steps for a role")
    public ResponseEntity<List<OnboardingStepDto>> getSteps(
            @RequestParam(defaultValue = "AGENT") UserOnboarding.UserRole role) {

        List<OnboardingStepDto> steps = onboardingService.getStepsForRole(role);
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/tours/{stepId}")
    @Operation(summary = "Get tour configuration for a step")
    public ResponseEntity<OnboardingStepDto> getTourConfig(@PathVariable String stepId) {
        return onboardingService.getTourConfig(stepId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    @Operation(summary = "Get onboarding statistics for tenant")
    public ResponseEntity<OnboardingService.OnboardingStatsDto> getStats(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {

        OnboardingService.OnboardingStatsDto stats = onboardingService.getStats(tenantId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/action")
    @Operation(summary = "Track user action for auto-completion")
    public ResponseEntity<Void> trackAction(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestBody ActionTrackingRequest request) {

        onboardingService.checkAutoCompletion(userId, request.getActionType(), request.getData());
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    public static class ActionTrackingRequest {
        private String actionType;
        private Map<String, Object> data;
    }
}
