package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeApproval extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequest changeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Column(name = "approval_level", nullable = false)
    @Builder.Default
    private Integer approvalLevel = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(columnDefinition = "TEXT")
    private String comments;

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        DELEGATED
    }
}
