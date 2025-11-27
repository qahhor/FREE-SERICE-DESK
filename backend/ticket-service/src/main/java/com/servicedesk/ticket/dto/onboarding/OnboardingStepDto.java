package com.servicedesk.ticket.dto.onboarding;

import com.servicedesk.ticket.entity.onboarding.OnboardingStep;
import com.servicedesk.ticket.entity.onboarding.UserOnboarding;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for onboarding step
 */
@Data
@Builder
public class OnboardingStepDto {

    private UUID id;
    private String stepId;
    private String title;
    private String description;
    private OnboardingStep.StepType type;
    private UserOnboarding.UserRole targetRole;
    private int displayOrder;
    private boolean required;
    private String icon;
    private String actionRoute;
    private String actionLabel;
    private String videoUrl;
    private String helpUrl;
    private int estimatedMinutes;
    private List<TourStepDto> tourSteps;

    // Completion status for current user
    private boolean completed;
    private String completedAt;

    public static OnboardingStepDto fromEntity(OnboardingStep entity) {
        return OnboardingStepDto.builder()
                .id(entity.getId())
                .stepId(entity.getStepId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .type(entity.getType())
                .targetRole(entity.getTargetRole())
                .displayOrder(entity.getDisplayOrder())
                .required(entity.isRequired())
                .icon(entity.getIcon())
                .actionRoute(entity.getActionRoute())
                .actionLabel(entity.getActionLabel())
                .videoUrl(entity.getVideoUrl())
                .helpUrl(entity.getHelpUrl())
                .estimatedMinutes(entity.getEstimatedMinutes())
                .tourSteps(entity.getTourConfig() != null
                        ? entity.getTourConfig().stream()
                        .map(TourStepDto::fromConfig)
                        .toList()
                        : List.of())
                .build();
    }

    @Data
    @Builder
    public static class TourStepDto {
        private String elementSelector;
        private String title;
        private String content;
        private String position;
        private String highlightClass;
        private boolean allowInteraction;
        private String nextButtonLabel;
        private String prevButtonLabel;

        public static TourStepDto fromConfig(OnboardingStep.TourStepConfig config) {
            return TourStepDto.builder()
                    .elementSelector(config.getElementSelector())
                    .title(config.getTitle())
                    .content(config.getContent())
                    .position(config.getPosition())
                    .highlightClass(config.getHighlightClass())
                    .allowInteraction(config.isAllowInteraction())
                    .nextButtonLabel(config.getNextButtonLabel())
                    .prevButtonLabel(config.getPrevButtonLabel())
                    .build();
        }
    }
}
