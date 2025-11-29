package com.servicedesk.monolith.ticket.entity.onboarding;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks user's onboarding progress
 */
@Entity
@Table(name = "user_onboarding")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OnboardingStatus status = OnboardingStatus.NOT_STARTED;

    @Column(name = "current_step")
    @Builder.Default
    private Integer currentStep = 0;

    @Column(name = "total_steps")
    @Builder.Default
    private Integer totalSteps = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completed_steps", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, StepCompletion> completedSteps = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tour_progress", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Boolean> tourProgress = new HashMap<>();

    @Column(name = "welcome_dismissed")
    @Builder.Default
    private boolean welcomeDismissed = false;

    @Column(name = "tour_completed")
    @Builder.Default
    private boolean tourCompleted = false;

    @Column(name = "checklist_completed")
    @Builder.Default
    private boolean checklistCompleted = false;

    @Column(name = "hints_enabled")
    @Builder.Default
    private boolean hintsEnabled = true;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum OnboardingStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED
    }

    public enum UserRole {
        AGENT,
        ADMIN,
        SUPERVISOR,
        CUSTOMER
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepCompletion {
        private String stepId;
        private boolean completed;
        private Instant completedAt;
        private Map<String, Object> metadata;
    }

    public int getCompletionPercentage() {
        if (totalSteps == 0) return 0;
        long completed = completedSteps.values().stream()
                .filter(StepCompletion::isCompleted)
                .count();
        return (int) ((completed * 100) / totalSteps);
    }

    public void markStepCompleted(String stepId) {
        StepCompletion completion = new StepCompletion();
        completion.setStepId(stepId);
        completion.setCompleted(true);
        completion.setCompletedAt(Instant.now());
        completedSteps.put(stepId, completion);

        if (status == OnboardingStatus.NOT_STARTED) {
            status = OnboardingStatus.IN_PROGRESS;
            startedAt = Instant.now();
        }

        if (getCompletionPercentage() == 100) {
            status = OnboardingStatus.COMPLETED;
            completedAt = Instant.now();
            checklistCompleted = true;
        }
    }
}
