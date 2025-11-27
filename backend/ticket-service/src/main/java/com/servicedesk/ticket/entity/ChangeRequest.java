package com.servicedesk.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "change_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRequest extends BaseEntity {

    @Column(name = "change_number", unique = true, nullable = false, length = 20)
    private String changeNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String justification;

    // Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 30)
    @Builder.Default
    private ChangeType changeType = ChangeType.NORMAL;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Ticket.TicketPriority priority = Ticket.TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ImpactLevel impact = ImpactLevel.MEDIUM;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ChangeStatus status = ChangeStatus.DRAFT;

    // People
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // Scheduling
    @Column(name = "scheduled_start")
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end")
    private LocalDateTime scheduledEnd;

    @Column(name = "actual_start")
    private LocalDateTime actualStart;

    @Column(name = "actual_end")
    private LocalDateTime actualEnd;

    // Implementation details
    @Column(name = "implementation_plan", columnDefinition = "TEXT")
    private String implementationPlan;

    @Column(name = "rollback_plan", columnDefinition = "TEXT")
    private String rollbackPlan;

    @Column(name = "test_plan", columnDefinition = "TEXT")
    private String testPlan;

    @Column(name = "communication_plan", columnDefinition = "TEXT")
    private String communicationPlan;

    // Post-implementation
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column
    private Boolean success;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChangeApproval> approvals = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "change_tickets",
            joinColumns = @JoinColumn(name = "change_request_id"),
            inverseJoinColumns = @JoinColumn(name = "ticket_id")
    )
    @Builder.Default
    private Set<Ticket> linkedTickets = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "change_assets",
            joinColumns = @JoinColumn(name = "change_request_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    @Builder.Default
    private Set<Asset> affectedAssets = new HashSet<>();

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<ChangeHistory> history = new ArrayList<>();

    public void addHistoryEntry(ChangeHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setChangeRequest(this);
    }

    public enum ChangeType {
        STANDARD,    // Pre-approved, low risk
        NORMAL,      // Requires CAB approval
        EMERGENCY    // Fast-track for critical issues
    }

    public enum ChangeStatus {
        DRAFT,
        SUBMITTED,
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        ROLLED_BACK,
        CLOSED,
        CANCELLED
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ImpactLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
