package com.servicedesk.monolith.ticket.entity.onboarding;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Defines an achievement that users can earn during onboarding
 */
@Entity
@Table(name = "achievements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "achievement_id", nullable = false, unique = true)
    private String achievementId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false)
    private AchievementType type;

    /**
     * Condition to earn this achievement (JSON expression)
     * Example: {"type": "step_completed", "stepId": "first_step"}
     * Example: {"type": "onboarding_completed"}
     * Example: {"type": "tours_viewed", "count": 3}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "earn_condition", columnDefinition = "jsonb")
    private Map<String, Object> earnCondition;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum AchievementType {
        FIRST_STEP,       // Earned when completing first onboarding step
        QUICK_LEARNER,    // Earned when completing entire onboarding
        EXPLORER,         // Earned when viewing all tour sections
        MILESTONE,        // Earned at specific milestones
        CUSTOM           // Custom achievement type
    }
}
