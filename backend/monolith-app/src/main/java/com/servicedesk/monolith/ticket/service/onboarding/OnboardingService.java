package com.servicedesk.monolith.ticket.service.onboarding;

import com.servicedesk.monolith.ticket.dto.onboarding.OnboardingProgressDto;
import com.servicedesk.monolith.ticket.dto.onboarding.OnboardingStepDto;
import com.servicedesk.monolith.ticket.entity.onboarding.OnboardingStep;
import com.servicedesk.monolith.ticket.entity.onboarding.UserOnboarding;
import com.servicedesk.monolith.ticket.repository.onboarding.OnboardingStepRepository;
import com.servicedesk.monolith.ticket.repository.onboarding.UserOnboardingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserOnboardingRepository onboardingRepository;
    private final OnboardingStepRepository stepRepository;

    /**
     * Get or create onboarding progress for user
     */
    @Transactional
    public OnboardingProgressDto getOrCreateProgress(UUID userId, UUID tenantId, UserOnboarding.UserRole role) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseGet(() -> createOnboarding(userId, tenantId, role));

        return buildProgressDto(onboarding);
    }

    /**
     * Get onboarding progress for user
     */
    @Transactional(readOnly = true)
    public Optional<OnboardingProgressDto> getProgress(UUID userId) {
        return onboardingRepository.findByUserId(userId)
                .map(this::buildProgressDto);
    }

    /**
     * Mark a step as completed
     */
    @Transactional
    public OnboardingProgressDto completeStep(UUID userId, String stepId) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found for user: " + userId));

        onboarding.markStepCompleted(stepId);
        onboarding.setCurrentStep(onboarding.getCurrentStep() + 1);
        onboarding = onboardingRepository.save(onboarding);

        log.info("User {} completed onboarding step: {}", userId, stepId);

        return buildProgressDto(onboarding);
    }

    /**
     * Mark tour as completed
     */
    @Transactional
    public OnboardingProgressDto completeTour(UUID userId, String tourId) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found for user: " + userId));

        onboarding.getTourProgress().put(tourId, true);

        // Check if all tours are completed
        List<OnboardingStep> tourSteps = stepRepository.findByTargetRoleAndType(
                onboarding.getUserRole(),
                OnboardingStep.StepType.TOUR
        );

        boolean allToursCompleted = tourSteps.stream()
                .allMatch(step -> onboarding.getTourProgress().getOrDefault(step.getStepId(), false));

        if (allToursCompleted) {
            onboarding.setTourCompleted(true);
        }

        onboarding = onboardingRepository.save(onboarding);

        log.info("User {} completed tour: {}", userId, tourId);

        return buildProgressDto(onboarding);
    }

    /**
     * Dismiss welcome wizard
     */
    @Transactional
    public OnboardingProgressDto dismissWelcome(UUID userId) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found for user: " + userId));

        onboarding.setWelcomeDismissed(true);
        onboarding = onboardingRepository.save(onboarding);

        return buildProgressDto(onboarding);
    }

    /**
     * Skip onboarding entirely
     */
    @Transactional
    public OnboardingProgressDto skipOnboarding(UUID userId) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found for user: " + userId));

        onboarding.setStatus(UserOnboarding.OnboardingStatus.SKIPPED);
        onboarding.setWelcomeDismissed(true);
        onboarding.setTourCompleted(true);
        onboarding.setCompletedAt(Instant.now());
        onboarding = onboardingRepository.save(onboarding);

        log.info("User {} skipped onboarding", userId);

        return buildProgressDto(onboarding);
    }

    /**
     * Toggle contextual hints
     */
    @Transactional
    public OnboardingProgressDto toggleHints(UUID userId, boolean enabled) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found for user: " + userId));

        onboarding.setHintsEnabled(enabled);
        onboarding = onboardingRepository.save(onboarding);

        return buildProgressDto(onboarding);
    }

    /**
     * Reset onboarding for user (admin action)
     */
    @Transactional
    public OnboardingProgressDto resetOnboarding(UUID userId) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding not found for user: " + userId));

        onboarding.setStatus(UserOnboarding.OnboardingStatus.NOT_STARTED);
        onboarding.setCurrentStep(0);
        onboarding.setCompletedSteps(new HashMap<>());
        onboarding.setTourProgress(new HashMap<>());
        onboarding.setWelcomeDismissed(false);
        onboarding.setTourCompleted(false);
        onboarding.setChecklistCompleted(false);
        onboarding.setStartedAt(null);
        onboarding.setCompletedAt(null);

        onboarding = onboardingRepository.save(onboarding);

        log.info("Onboarding reset for user {}", userId);

        return buildProgressDto(onboarding);
    }

    /**
     * Get steps for a specific role
     */
    @Transactional(readOnly = true)
    public List<OnboardingStepDto> getStepsForRole(UserOnboarding.UserRole role) {
        return stepRepository.findByTargetRoleOrderByDisplayOrder(role).stream()
                .map(OnboardingStepDto::fromEntity)
                .toList();
    }

    /**
     * Get tour configuration for a step
     */
    @Transactional(readOnly = true)
    public Optional<OnboardingStepDto> getTourConfig(String stepId) {
        return stepRepository.findByStepId(stepId)
                .map(OnboardingStepDto::fromEntity);
    }

    /**
     * Auto-complete steps based on user actions
     */
    @Transactional
    public void checkAutoCompletion(UUID userId, String actionType, Map<String, Object> actionData) {
        UserOnboarding onboarding = onboardingRepository.findByUserId(userId)
                .orElse(null);

        if (onboarding == null || onboarding.getStatus() == UserOnboarding.OnboardingStatus.COMPLETED
                || onboarding.getStatus() == UserOnboarding.OnboardingStatus.SKIPPED) {
            return;
        }

        List<OnboardingStep> steps = stepRepository.findByTargetRoleOrderByDisplayOrder(onboarding.getUserRole());

        for (OnboardingStep step : steps) {
            if (onboarding.getCompletedSteps().containsKey(step.getStepId())) {
                continue; // Already completed
            }

            Map<String, Object> condition = step.getCompletionCondition();
            if (condition != null && matchesCondition(condition, actionType, actionData)) {
                onboarding.markStepCompleted(step.getStepId());
                log.info("Auto-completed step {} for user {} based on action {}", step.getStepId(), userId, actionType);
            }
        }

        onboardingRepository.save(onboarding);
    }

    /**
     * Get onboarding statistics for tenant
     */
    @Transactional(readOnly = true)
    public OnboardingStatsDto getStats(UUID tenantId) {
        List<UserOnboarding> allOnboarding = onboardingRepository.findByTenantId(tenantId);

        long total = allOnboarding.size();
        long completed = allOnboarding.stream()
                .filter(o -> o.getStatus() == UserOnboarding.OnboardingStatus.COMPLETED)
                .count();
        long inProgress = allOnboarding.stream()
                .filter(o -> o.getStatus() == UserOnboarding.OnboardingStatus.IN_PROGRESS)
                .count();
        long skipped = allOnboarding.stream()
                .filter(o -> o.getStatus() == UserOnboarding.OnboardingStatus.SKIPPED)
                .count();

        double avgProgress = allOnboarding.stream()
                .mapToInt(UserOnboarding::getCompletionPercentage)
                .average()
                .orElse(0.0);

        return OnboardingStatsDto.builder()
                .totalUsers(total)
                .completedCount(completed)
                .inProgressCount(inProgress)
                .skippedCount(skipped)
                .averageProgress(avgProgress)
                .completionRate(total > 0 ? (completed * 100.0 / total) : 0)
                .build();
    }

    private UserOnboarding createOnboarding(UUID userId, UUID tenantId, UserOnboarding.UserRole role) {
        int totalSteps = stepRepository.countRequiredStepsByRole(role);

        UserOnboarding onboarding = UserOnboarding.builder()
                .userId(userId)
                .tenantId(tenantId)
                .userRole(role)
                .status(UserOnboarding.OnboardingStatus.NOT_STARTED)
                .currentStep(0)
                .totalSteps(totalSteps)
                .completedSteps(new HashMap<>())
                .tourProgress(new HashMap<>())
                .build();

        onboarding = onboardingRepository.save(onboarding);

        log.info("Created onboarding for user {} with role {} ({} steps)", userId, role, totalSteps);

        return onboarding;
    }

    private OnboardingProgressDto buildProgressDto(UserOnboarding onboarding) {
        List<OnboardingStep> steps = stepRepository.findByTargetRoleOrderByDisplayOrder(onboarding.getUserRole());

        List<OnboardingStepDto> stepDtos = steps.stream()
                .map(step -> {
                    OnboardingStepDto dto = OnboardingStepDto.fromEntity(step);
                    UserOnboarding.StepCompletion completion = onboarding.getCompletedSteps().get(step.getStepId());
                    if (completion != null) {
                        dto.setCompleted(completion.isCompleted());
                        dto.setCompletedAt(completion.getCompletedAt() != null
                                ? completion.getCompletedAt().toString()
                                : null);
                    }
                    return dto;
                })
                .toList();

        // Find next step
        OnboardingStepDto nextStep = stepDtos.stream()
                .filter(s -> !s.isCompleted())
                .findFirst()
                .orElse(null);

        Map<String, Boolean> completedStepIds = onboarding.getCompletedSteps().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().isCompleted()
                ));

        return OnboardingProgressDto.builder()
                .userId(onboarding.getUserId())
                .userRole(onboarding.getUserRole())
                .status(onboarding.getStatus())
                .currentStep(onboarding.getCurrentStep())
                .totalSteps(onboarding.getTotalSteps())
                .completionPercentage(onboarding.getCompletionPercentage())
                .steps(stepDtos)
                .completedStepIds(completedStepIds)
                .welcomeDismissed(onboarding.isWelcomeDismissed())
                .tourCompleted(onboarding.isTourCompleted())
                .checklistCompleted(onboarding.isChecklistCompleted())
                .hintsEnabled(onboarding.isHintsEnabled())
                .startedAt(onboarding.getStartedAt())
                .completedAt(onboarding.getCompletedAt())
                .nextStep(nextStep)
                .achievements(List.of()) // TODO: Implement achievements
                .build();
    }

    private boolean matchesCondition(Map<String, Object> condition, String actionType, Map<String, Object> actionData) {
        String requiredType = (String) condition.get("type");
        if (!actionType.equals(requiredType)) {
            return false;
        }

        // Check count condition
        Integer requiredCount = (Integer) condition.get("count");
        if (requiredCount != null) {
            Integer actualCount = (Integer) actionData.getOrDefault("count", 1);
            return actualCount >= requiredCount;
        }

        return true;
    }

    @lombok.Data
    @lombok.Builder
    public static class OnboardingStatsDto {
        private long totalUsers;
        private long completedCount;
        private long inProgressCount;
        private long skippedCount;
        private double averageProgress;
        private double completionRate;
    }
}
