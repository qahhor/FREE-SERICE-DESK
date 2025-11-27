package com.servicedesk.ticket.dto;

import com.servicedesk.ticket.entity.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProblemDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        private String category;
        private Ticket.TicketPriority priority;
        private ChangeRequest.ImpactLevel impact;
        private Problem.UrgencyLevel urgency;
        private String assigneeId;
        private String teamId;
        private String projectId;
        private List<String> linkedIncidentIds;
        private List<String> affectedAssetIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
        private String category;
        private Ticket.TicketPriority priority;
        private ChangeRequest.ImpactLevel impact;
        private Problem.UrgencyLevel urgency;
        private String assigneeId;
        private String teamId;
        private String rootCause;
        private Problem.RootCauseCategory rootCauseCategory;
        private String workaround;
        private String solution;
        private BigDecimal estimatedImpactCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String problemNumber;
        private String title;
        private String description;
        private String category;
        private Ticket.TicketPriority priority;
        private ChangeRequest.ImpactLevel impact;
        private Problem.UrgencyLevel urgency;
        private Problem.ProblemStatus status;
        private UserSummary reportedBy;
        private UserSummary assignee;
        private TeamSummary team;
        private String rootCause;
        private Problem.RootCauseCategory rootCauseCategory;
        private LocalDateTime rcaCompletedAt;
        private UserSummary rcaCompletedBy;
        private String workaround;
        private Boolean workaroundAvailable;
        private String solution;
        private Boolean solutionVerified;
        private LocalDateTime solutionVerifiedAt;
        private UserSummary solutionVerifiedBy;
        private Integer incidentCount;
        private BigDecimal estimatedImpactCost;
        private String projectId;
        private KnownErrorSummary knownError;
        private Integer linkedIncidentsCount;
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
        private String problemNumber;
        private String title;
        private String category;
        private Ticket.TicketPriority priority;
        private Problem.ProblemStatus status;
        private String assigneeName;
        private Integer incidentCount;
        private Boolean workaroundAvailable;
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
    public static class KnownErrorSummary {
        private String id;
        private String errorNumber;
        private String title;
    }

    // Known Error DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnownErrorCreateRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        private String symptoms;
        private String rootCause;
        private String workaround;
        private String permanentFix;
        private String affectedServices;
        private String affectedSystems;
        private String category;
        private Ticket.TicketPriority priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnownErrorResponse {
        private String id;
        private String errorNumber;
        private String title;
        private String description;
        private String problemId;
        private String problemNumber;
        private String symptoms;
        private String rootCause;
        private String workaround;
        private String permanentFix;
        private KnownError.ErrorStatus status;
        private Boolean fixAvailable;
        private Boolean fixImplemented;
        private LocalDateTime fixImplementedAt;
        private String affectedServices;
        private String affectedSystems;
        private Integer incidentCount;
        private String category;
        private Ticket.TicketPriority priority;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // RCA DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RcaRequest {
        private String templateId;
        private Map<String, Object> analysisData;
        private String findings;
        private String recommendations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RcaResponse {
        private String id;
        private String problemId;
        private String templateId;
        private String templateName;
        private Map<String, Object> analysisData;
        private String findings;
        private String recommendations;
        private String conductedById;
        private String conductedByName;
        private LocalDateTime conductedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RcaTemplateResponse {
        private String id;
        private String name;
        private String description;
        private RcaTemplate.TemplateType templateType;
        private Map<String, Object> questions;
        private Boolean isDefault;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdate {
        private Problem.ProblemStatus status;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkaroundUpdate {
        private String workaround;
        private Boolean workaroundAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolutionUpdate {
        private String solution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemMetrics {
        private Long totalProblems;
        private Long openProblems;
        private Long withWorkaround;
        private Long withRootCause;
        private Long resolved;
        private Long knownErrors;
        private Long activeKnownErrors;
        private Double avgResolutionDays;
        private Integer totalLinkedIncidents;
        private Map<String, Long> byCategory;
        private Map<String, Long> byRootCauseCategory;
    }
}
