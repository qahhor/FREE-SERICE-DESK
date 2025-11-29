package com.servicedesk.monolith.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a user review for a module
 */
@Entity
@Table(name = "module_reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"module_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "module_id", nullable = false)
    private String moduleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "installed_version")
    private String installedVersion;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "reported")
    @Builder.Default
    private boolean reported = false;

    @Column(name = "verified_purchase")
    @Builder.Default
    private boolean verifiedPurchase = false;

    @Column(name = "author_response", columnDefinition = "TEXT")
    private String authorResponse;

    @Column(name = "author_response_at")
    private Instant authorResponseAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum ReviewStatus {
        PUBLISHED,
        HIDDEN,
        FLAGGED
    }
}
