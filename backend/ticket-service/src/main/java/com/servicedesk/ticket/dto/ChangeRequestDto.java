package com.servicedesk.ticket.dto;

import com.servicedesk.ticket.entity.ChangeApproval;
import com.servicedesk.ticket.entity.ChangeRequest;
import com.servicedesk.ticket.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ChangeRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        private String justification;
        private ChangeRequest.ChangeType changeType;
        private String category;
        private Ticket.TicketPriority priority;
        private ChangeRequest.RiskLevel riskLevel;
        private ChangeRequest.ImpactLevel impact;
        private String assigneeId;
        private String teamId;
        private LocalDateTime scheduledStart;
        private LocalDateTime scheduledEnd;
        private String implementationPlan;
        private String rollbackPlan;
        private String testPlan;
        private String communicationPlan;
        private String projectId;
        private List<String> affectedAssetIds;
        private List<String> relatedTicketIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
        private String justification;
        private ChangeRequest.ChangeType changeType;
        private String category;
        private Ticket.TicketPriority priority;
        private ChangeRequest.RiskLevel riskLevel;
        private ChangeRequest.ImpactLevel impact;
        private String assigneeId;
        private String teamId;
        private LocalDateTime scheduledStart;
        private LocalDateTime scheduledEnd;
        private String implementationPlan;
        private String rollbackPlan;
        private String testPlan;
        private String communicationPlan;
        private List<String> affectedAssetIds;
        private List<String> relatedTicketIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String changeNumber;
        private String title;
        private String description;
        private String justification;
        private ChangeRequest.ChangeType changeType;
        private String category;
        private Ticket.TicketPriority priority;
        private ChangeRequest.RiskLevel riskLevel;
        private ChangeRequest.ImpactLevel impact;
        private ChangeRequest.ChangeStatus status;
        private UserSummary requester;
        private UserSummary assignee;
        private TeamSummary team;
        private LocalDateTime scheduledStart;
        private LocalDateTime scheduledEnd;
        private LocalDateTime actualStart;
        private LocalDateTime actualEnd;
        private String implementationPlan;
        private String rollbackPlan;
        private String testPlan;
        private String communicationPlan;
        private String reviewNotes;
        private Boolean success;
        private String projectId;
        private List<ApprovalResponse> approvals;
        private Integer linkedTicketsCount;
        private Integer affectedAssetsCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private String id;
        private String changeNumber;
        private String title;
        private ChangeRequest.ChangeType changeType;
        private Ticket.TicketPriority priority;
        private ChangeRequest.RiskLevel riskLevel;
        private ChangeRequest.ChangeStatus status;
        private String requesterName;
        private String assigneeName;
        private LocalDateTime scheduledStart;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private String id;
        private String email;
        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamSummary {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalRequest {
        private String approverId;
        private Integer approvalLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalResponse {
        private String id;
        private String approverId;
        private String approverName;
        private Integer approvalLevel;
        private ChangeApproval.ApprovalStatus status;
        private LocalDateTime decisionDate;
        private String comments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalDecision {
        private ChangeApproval.ApprovalStatus decision;
        private String comments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdate {
        private ChangeRequest.ChangeStatus status;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewRequest {
        private Boolean success;
        private String reviewNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeMetrics {
        private Long totalChanges;
        private Long pendingApproval;
        private Long scheduled;
        private Long inProgress;
        private Long completedSuccessfully;
        private Long failed;
        private Long rolledBack;
        private Double successRate;
        private Long emergencyChanges;
        private Long standardChanges;
        private Long normalChanges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEvent {
        private String id;
        private String changeNumber;
        private String title;
        private ChangeRequest.ChangeType type;
        private ChangeRequest.ChangeStatus status;
        private LocalDateTime start;
        private LocalDateTime end;
        private String color;
    }
}
