package com.servicedesk.monolith.ticket.dto;

import com.servicedesk.monolith.ticket.entity.EscalationRule;
import com.servicedesk.monolith.ticket.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class EscalationRuleDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private String slaPolicyId;
        @NotNull(message = "Trigger type is required")
        private EscalationRule.TriggerType triggerType;
        private Integer triggerMinutes;
        private Integer escalationLevel;
        private Boolean notifyAssignee;
        private Boolean notifyTeamLead;
        private Boolean notifyManager;
        private List<String> notifyCustomUserIds;
        private String notifyEmail;
        private String reassignToUserId;
        private String reassignToTeamId;
        private Ticket.TicketPriority changePriority;
        private List<String> addTags;
        private Boolean isActive;
        private Integer executionOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String description;
        private String slaPolicyId;
        private EscalationRule.TriggerType triggerType;
        private Integer triggerMinutes;
        private Integer escalationLevel;
        private Boolean notifyAssignee;
        private Boolean notifyTeamLead;
        private Boolean notifyManager;
        private List<String> notifyCustomUserIds;
        private String notifyEmail;
        private String reassignToUserId;
        private String reassignToTeamId;
        private Ticket.TicketPriority changePriority;
        private List<String> addTags;
        private Boolean isActive;
        private Integer executionOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String description;
        private String slaPolicyId;
        private String slaPolicyName;
        private EscalationRule.TriggerType triggerType;
        private Integer triggerMinutes;
        private Integer escalationLevel;
        private Boolean notifyAssignee;
        private Boolean notifyTeamLead;
        private Boolean notifyManager;
        private List<String> notifyCustomUserIds;
        private String notifyEmail;
        private String reassignToUserId;
        private String reassignToUserName;
        private String reassignToTeamId;
        private String reassignToTeamName;
        private Ticket.TicketPriority changePriority;
        private List<String> addTags;
        private Boolean isActive;
        private Integer executionOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscalationEvent {
        private String ticketId;
        private String ticketNumber;
        private String ruleId;
        private String ruleName;
        private EscalationRule.TriggerType triggerType;
        private Integer escalationLevel;
        private List<String> notifiedUsers;
        private String newAssigneeId;
        private String newTeamId;
        private Ticket.TicketPriority newPriority;
        private LocalDateTime escalatedAt;
    }
}
