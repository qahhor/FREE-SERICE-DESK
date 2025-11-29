package com.servicedesk.monolith.ticket.entity.onboarding;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Defines an onboarding step/task
 */
@Entity
@Table(name = "onboarding_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "step_id", nullable = false, unique = true)
    private String stepId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false)
    private UserOnboarding.UserRole targetRole;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_required")
    @Builder.Default
    private boolean required = true;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    /**
     * Icon name (Material Icons)
     */
    private String icon;

    /**
     * Route to navigate when clicking this step
     */
    @Column(name = "action_route")
    private String actionRoute;

    /**
     * Action button label
     */
    @Column(name = "action_label")
    private String actionLabel;

    /**
     * URL to video tutorial
     */
    @Column(name = "video_url")
    private String videoUrl;

    /**
     * URL to help documentation
     */
    @Column(name = "help_url")
    private String helpUrl;

    /**
     * Estimated time in minutes
     */
    @Column(name = "estimated_minutes")
    @Builder.Default
    private Integer estimatedMinutes = 2;

    /**
     * Condition to auto-complete this step (JSON expression)
     * Example: {"type": "ticket_created", "count": 1}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completion_condition", columnDefinition = "jsonb")
    private Map<String, Object> completionCondition;

    /**
     * Tour steps for guided tour (JSON array)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tour_config", columnDefinition = "jsonb")
    private List<TourStepConfig> tourConfig;

    public enum StepType {
        CHECKLIST,      // Simple checkbox task
        TOUR,           // Interactive guided tour
        ACTION,         // Requires user action (create ticket, etc.)
        VIDEO,          // Watch a video
        LINK,           // Read documentation
        CUSTOM          // Custom component
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourStepConfig {
        private String elementSelector;
        private String title;
        private String content;
        private String position; // top, bottom, left, right
        private String highlightClass;
        private boolean allowInteraction;
        private String nextButtonLabel;
        private String prevButtonLabel;
    }
}
