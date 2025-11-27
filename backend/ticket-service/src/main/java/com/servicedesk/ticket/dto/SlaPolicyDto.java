package com.servicedesk.ticket.dto;

import com.servicedesk.ticket.entity.SlaCondition;
import com.servicedesk.ticket.entity.SlaTarget;
import com.servicedesk.ticket.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class SlaPolicyDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private String projectId;
        private Boolean enabled;
        private Boolean isDefault;
        private Integer priorityOrder;
        private Boolean businessHoursOnly;
        private String businessHoursStart;
        private String businessHoursEnd;
        private String businessDays;
        private String timezone;
        private List<TargetRequest> targets;
        private List<ConditionRequest> conditions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String description;
        private Boolean enabled;
        private Boolean isDefault;
        private Integer priorityOrder;
        private Boolean businessHoursOnly;
        private String businessHoursStart;
        private String businessHoursEnd;
        private String businessDays;
        private String timezone;
        private List<TargetRequest> targets;
        private List<ConditionRequest> conditions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String description;
        private String projectId;
        private Boolean enabled;
        private Boolean isDefault;
        private Integer priorityOrder;
        private Boolean businessHoursOnly;
        private String businessHoursStart;
        private String businessHoursEnd;
        private String businessDays;
        private String timezone;
        private List<TargetResponse> targets;
        private List<ConditionResponse> conditions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetRequest {
        private SlaTarget.TargetType targetType;
        private Ticket.TicketPriority priority;
        private Integer targetMinutes;
        private Integer warningMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetResponse {
        private String id;
        private SlaTarget.TargetType targetType;
        private Ticket.TicketPriority priority;
        private Integer targetMinutes;
        private Integer warningMinutes;
        private String targetFormatted; // e.g., "4h", "24h", "30m"
        private String warningFormatted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionRequest {
        private String field;
        private SlaCondition.Operator operator;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionResponse {
        private String id;
        private String field;
        private SlaCondition.Operator operator;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaStatus {
        private String ticketId;
        private String ticketNumber;
        private String policyName;
        private LocalDateTime firstResponseDue;
        private LocalDateTime resolutionDue;
        private Boolean firstResponseBreached;
        private Boolean resolutionBreached;
        private Boolean slaBreached;
        private Long firstResponseRemainingMinutes;
        private Long resolutionRemainingMinutes;
        private String firstResponseStatus; // ON_TRACK, AT_RISK, BREACHED
        private String resolutionStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaMetrics {
        private Long totalTickets;
        private Long ticketsWithSla;
        private Long firstResponseWithinSla;
        private Long firstResponseBreached;
        private Long resolutionWithinSla;
        private Long resolutionBreached;
        private Double firstResponseComplianceRate;
        private Double resolutionComplianceRate;
        private Double overallComplianceRate;
        private Double avgFirstResponseTime; // in minutes
        private Double avgResolutionTime; // in minutes
    }
}
