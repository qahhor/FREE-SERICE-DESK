package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "known_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnownError extends BaseEntity {

    @Column(name = "error_number", unique = true, nullable = false, length = 20)
    private String errorNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Origin
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    // Error details
    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String workaround;

    @Column(name = "permanent_fix", columnDefinition = "TEXT")
    private String permanentFix;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ErrorStatus status = ErrorStatus.ACTIVE;

    @Column(name = "fix_available")
    @Builder.Default
    private Boolean fixAvailable = false;

    @Column(name = "fix_implemented")
    @Builder.Default
    private Boolean fixImplemented = false;

    @Column(name = "fix_implemented_at")
    private LocalDateTime fixImplementedAt;

    // Impact
    @Column(name = "affected_services", columnDefinition = "TEXT")
    private String affectedServices;

    @Column(name = "affected_systems", columnDefinition = "TEXT")
    private String affectedSystems;

    @Column(name = "incident_count")
    @Builder.Default
    private Integer incidentCount = 0;

    // Classification
    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Ticket.TicketPriority priority = Ticket.TicketPriority.MEDIUM;

    public enum ErrorStatus {
        ACTIVE,
        RESOLVED,
        ARCHIVED
    }
}
