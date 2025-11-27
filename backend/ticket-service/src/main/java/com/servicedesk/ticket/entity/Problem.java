package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem extends BaseEntity {

    @Column(name = "problem_number", unique = true, nullable = false, length = 20)
    private String problemNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Classification
    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Ticket.TicketPriority priority = Ticket.TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ChangeRequest.ImpactLevel impact = ChangeRequest.ImpactLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private UrgencyLevel urgency = UrgencyLevel.MEDIUM;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProblemStatus status = ProblemStatus.IDENTIFIED;

    // People
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // Root Cause Analysis
    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Enumerated(EnumType.STRING)
    @Column(name = "root_cause_category", length = 50)
    private RootCauseCategory rootCauseCategory;

    @Column(name = "rca_completed_at")
    private LocalDateTime rcaCompletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rca_completed_by")
    private User rcaCompletedBy;

    // Solution
    @Column(columnDefinition = "TEXT")
    private String workaround;

    @Column(name = "workaround_available")
    @Builder.Default
    private Boolean workaroundAvailable = false;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(name = "solution_verified")
    @Builder.Default
    private Boolean solutionVerified = false;

    @Column(name = "solution_verified_at")
    private LocalDateTime solutionVerifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_verified_by")
    private User solutionVerifiedBy;

    // Metrics
    @Column(name = "incident_count")
    @Builder.Default
    private Integer incidentCount = 0;

    @Column(name = "estimated_impact_cost", precision = 12, scale = 2)
    private BigDecimal estimatedImpactCost;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "known_error_id")
    private KnownError knownError;

    @ManyToMany
    @JoinTable(
            name = "problem_incidents",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "ticket_id")
    )
    @Builder.Default
    private Set<Ticket> linkedIncidents = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "problem_assets",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    @Builder.Default
    private Set<Asset> affectedAssets = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<ProblemHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProblemRca> rcaRecords = new ArrayList<>();

    public void addHistoryEntry(ProblemHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setProblem(this);
    }

    public void addRcaRecord(ProblemRca rca) {
        rcaRecords.add(rca);
        rca.setProblem(this);
    }

    public enum ProblemStatus {
        IDENTIFIED,
        LOGGED,
        DIAGNOSED,
        WORKAROUND_AVAILABLE,
        ROOT_CAUSE_IDENTIFIED,
        SOLUTION_FOUND,
        CLOSED,
        VERIFIED
    }

    public enum UrgencyLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum RootCauseCategory {
        HARDWARE,
        SOFTWARE,
        PROCESS,
        PEOPLE,
        ENVIRONMENT,
        EXTERNAL,
        UNKNOWN
    }
}
