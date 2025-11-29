package com.servicedesk.monolith.ticket.dto.onboarding;

import com.servicedesk.monolith.ticket.entity.onboarding.UserOnboarding;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for user's onboarding progress
 */
@Data
@Builder
public class OnboardingProgressDto {

    private UUID userId;
    private UserOnboarding.UserRole userRole;
    private UserOnboarding.OnboardingStatus status;
    private int currentStep;
    private int totalSteps;
    private int completionPercentage;
    private List<OnboardingStepDto> steps;
    private Map<String, Boolean> completedStepIds;
    private boolean welcomeDismissed;
    private boolean tourCompleted;
    private boolean checklistCompleted;
    private boolean hintsEnabled;
    private Instant startedAt;
    private Instant completedAt;

    /**
     * Achievements earned during onboarding
     */
    private List<AchievementDto> achievements;

    /**
     * Next recommended step
     */
    private OnboardingStepDto nextStep;

    @Data
    @Builder
    public static class AchievementDto {
        private String id;
        private String title;
        private String description;
        private String icon;
        private Instant earnedAt;
    }
}
