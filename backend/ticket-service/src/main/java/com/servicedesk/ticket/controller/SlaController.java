package com.servicedesk.ticket.controller;

import com.servicedesk.ticket.dto.EscalationRuleDto;
import com.servicedesk.ticket.dto.SlaPolicyDto;
import com.servicedesk.ticket.entity.*;
import com.servicedesk.ticket.repository.*;
import com.servicedesk.ticket.service.EscalationService;
import com.servicedesk.ticket.service.SlaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sla")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SlaController {

    private final SlaPolicyRepository slaPolicyRepository;
    private final TicketRepository ticketRepository;
    private final EscalationRuleRepository escalationRuleRepository;
    private final SlaBreachHistoryRepository slaBreachHistoryRepository;
    private final BusinessHolidayRepository businessHolidayRepository;
    private final SlaService slaService;
    private final EscalationService escalationService;

    // ==================== SLA Policies ====================

    @GetMapping("/policies")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<SlaPolicyDto.Response>> getAllPolicies(
            @RequestParam(required = false) String projectId) {

        List<SlaPolicy> policies = projectId != null
                ? slaPolicyRepository.findByProjectId(projectId)
                : slaPolicyRepository.findAll();

        List<SlaPolicyDto.Response> response = policies.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/policies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<SlaPolicyDto.Response> getPolicy(@PathVariable String id) {
        return slaPolicyRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/policies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlaPolicyDto.Response> createPolicy(
            @Valid @RequestBody SlaPolicyDto.CreateRequest request) {

        SlaPolicy policy = SlaPolicy.builder()
                .name(request.getName())
                .description(request.getDescription())
                .projectId(request.getProjectId())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .priorityOrder(request.getPriorityOrder() != null ? request.getPriorityOrder() : 0)
                .businessHoursOnly(request.getBusinessHoursOnly() != null ? request.getBusinessHoursOnly() : true)
                .businessHoursStart(request.getBusinessHoursStart() != null ? request.getBusinessHoursStart() : "09:00")
                .businessHoursEnd(request.getBusinessHoursEnd() != null ? request.getBusinessHoursEnd() : "18:00")
                .businessDays(request.getBusinessDays() != null ? request.getBusinessDays() : "1,2,3,4,5")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .build();

        // Add targets
        if (request.getTargets() != null) {
            for (SlaPolicyDto.TargetRequest targetReq : request.getTargets()) {
                SlaTarget target = SlaTarget.builder()
                        .policy(policy)
                        .targetType(targetReq.getTargetType())
                        .priority(targetReq.getPriority())
                        .targetMinutes(targetReq.getTargetMinutes())
                        .warningMinutes(targetReq.getWarningMinutes())
                        .build();
                policy.getTargets().add(target);
            }
        }

        // Add conditions
        if (request.getConditions() != null) {
            for (SlaPolicyDto.ConditionRequest condReq : request.getConditions()) {
                SlaCondition condition = SlaCondition.builder()
                        .policy(policy)
                        .field(condReq.getField())
                        .operator(condReq.getOperator())
                        .value(condReq.getValue())
                        .build();
                policy.getConditions().add(condition);
            }
        }

        SlaPolicy saved = slaPolicyRepository.save(policy);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlaPolicyDto.Response> updatePolicy(
            @PathVariable String id,
            @Valid @RequestBody SlaPolicyDto.UpdateRequest request) {

        return slaPolicyRepository.findById(id)
                .map(policy -> {
                    if (request.getName() != null) policy.setName(request.getName());
                    if (request.getDescription() != null) policy.setDescription(request.getDescription());
                    if (request.getEnabled() != null) policy.setEnabled(request.getEnabled());
                    if (request.getIsDefault() != null) policy.setIsDefault(request.getIsDefault());
                    if (request.getPriorityOrder() != null) policy.setPriorityOrder(request.getPriorityOrder());
                    if (request.getBusinessHoursOnly() != null) policy.setBusinessHoursOnly(request.getBusinessHoursOnly());
                    if (request.getBusinessHoursStart() != null) policy.setBusinessHoursStart(request.getBusinessHoursStart());
                    if (request.getBusinessHoursEnd() != null) policy.setBusinessHoursEnd(request.getBusinessHoursEnd());
                    if (request.getBusinessDays() != null) policy.setBusinessDays(request.getBusinessDays());
                    if (request.getTimezone() != null) policy.setTimezone(request.getTimezone());

                    // Update targets if provided
                    if (request.getTargets() != null) {
                        policy.getTargets().clear();
                        for (SlaPolicyDto.TargetRequest targetReq : request.getTargets()) {
                            SlaTarget target = SlaTarget.builder()
                                    .policy(policy)
                                    .targetType(targetReq.getTargetType())
                                    .priority(targetReq.getPriority())
                                    .targetMinutes(targetReq.getTargetMinutes())
                                    .warningMinutes(targetReq.getWarningMinutes())
                                    .build();
                            policy.getTargets().add(target);
                        }
                    }

                    // Update conditions if provided
                    if (request.getConditions() != null) {
                        policy.getConditions().clear();
                        for (SlaPolicyDto.ConditionRequest condReq : request.getConditions()) {
                            SlaCondition condition = SlaCondition.builder()
                                    .policy(policy)
                                    .field(condReq.getField())
                                    .operator(condReq.getOperator())
                                    .value(condReq.getValue())
                                    .build();
                            policy.getConditions().add(condition);
                        }
                    }

                    return ResponseEntity.ok(toResponse(slaPolicyRepository.save(policy)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/policies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePolicy(@PathVariable String id) {
        return slaPolicyRepository.findById(id)
                .map(policy -> {
                    policy.setDeleted(true);
                    slaPolicyRepository.save(policy);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== SLA Status ====================

    @GetMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<SlaPolicyDto.SlaStatus> getTicketSlaStatus(@PathVariable UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> {
                    LocalDateTime now = LocalDateTime.now();

                    SlaPolicyDto.SlaStatus status = SlaPolicyDto.SlaStatus.builder()
                            .ticketId(ticket.getId())
                            .ticketNumber(ticket.getTicketNumber())
                            .policyName(ticket.getSlaPolicy() != null ? ticket.getSlaPolicy().getName() : null)
                            .firstResponseDue(ticket.getFirstResponseDue())
                            .resolutionDue(ticket.getResolutionDue())
                            .firstResponseBreached(ticket.getFirstResponseBreached())
                            .resolutionBreached(ticket.getResolutionBreached())
                            .slaBreached(ticket.getSlaBreached())
                            .firstResponseRemainingMinutes(calculateRemainingMinutes(ticket.getFirstResponseDue(), ticket.getFirstResponseAt(), now))
                            .resolutionRemainingMinutes(calculateRemainingMinutes(ticket.getResolutionDue(), ticket.getResolvedAt(), now))
                            .firstResponseStatus(calculateSlaStatus(ticket.getFirstResponseDue(), ticket.getFirstResponseAt(), ticket.getFirstResponseBreached(), now))
                            .resolutionStatus(calculateSlaStatus(ticket.getResolutionDue(), ticket.getResolvedAt(), ticket.getResolutionBreached(), now))
                            .build();

                    return ResponseEntity.ok(status);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<SlaPolicyDto.SlaMetrics> getSlaMetrics(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();

        long firstResponseWithinSla = ticketRepository.countFirstResponseWithinSla(startDate, endDate);
        long resolutionWithinSla = ticketRepository.countResolutionWithinSla(startDate, endDate);
        long breached = ticketRepository.countBreachedTickets(startDate, endDate);

        // This is simplified - in production you'd want more sophisticated queries
        long totalTickets = ticketRepository.count();

        SlaPolicyDto.SlaMetrics metrics = SlaPolicyDto.SlaMetrics.builder()
                .totalTickets(totalTickets)
                .ticketsWithSla(firstResponseWithinSla + breached)
                .firstResponseWithinSla(firstResponseWithinSla)
                .firstResponseBreached(breached)
                .resolutionWithinSla(resolutionWithinSla)
                .resolutionBreached(breached)
                .firstResponseComplianceRate(calculateComplianceRate(firstResponseWithinSla, breached))
                .resolutionComplianceRate(calculateComplianceRate(resolutionWithinSla, breached))
                .overallComplianceRate(calculateComplianceRate(firstResponseWithinSla + resolutionWithinSla, breached * 2))
                .build();

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/breaches")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Page<Map<String, Object>>> getBreachedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Ticket> breached = ticketRepository.findBreachedTickets(PageRequest.of(page, size));

        Page<Map<String, Object>> response = breached.map(ticket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("ticketId", ticket.getId());
            map.put("ticketNumber", ticket.getTicketNumber());
            map.put("subject", ticket.getSubject());
            map.put("status", ticket.getStatus());
            map.put("priority", ticket.getPriority());
            map.put("assignee", ticket.getAssignee() != null ? ticket.getAssignee().getEmail() : null);
            map.put("firstResponseBreached", ticket.getFirstResponseBreached());
            map.put("resolutionBreached", ticket.getResolutionBreached());
            map.put("createdAt", ticket.getCreatedAt());
            return map;
        });

        return ResponseEntity.ok(response);
    }

    // ==================== Escalation Rules ====================

    @GetMapping("/escalation-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EscalationRuleDto.Response>> getEscalationRules(
            @RequestParam(required = false) UUID policyId) {

        List<EscalationRule> rules = policyId != null
                ? escalationService.getRulesByPolicy(policyId)
                : escalationService.getAllActiveRules();

        List<EscalationRuleDto.Response> response = rules.stream()
                .map(this::toEscalationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/escalation-rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EscalationRuleDto.Response> getEscalationRule(@PathVariable UUID id) {
        return escalationService.getRule(id)
                .map(this::toEscalationResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/escalation-rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EscalationRuleDto.Response> createEscalationRule(
            @Valid @RequestBody EscalationRuleDto.CreateRequest request) {

        EscalationRule rule = EscalationRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .triggerType(request.getTriggerType())
                .triggerMinutes(request.getTriggerMinutes())
                .escalationLevel(request.getEscalationLevel() != null ? request.getEscalationLevel() : 1)
                .notifyAssignee(request.getNotifyAssignee() != null ? request.getNotifyAssignee() : true)
                .notifyTeamLead(request.getNotifyTeamLead() != null ? request.getNotifyTeamLead() : false)
                .notifyManager(request.getNotifyManager() != null ? request.getNotifyManager() : false)
                .notifyCustomUsers(request.getNotifyCustomUserIds() != null ?
                        String.join(",", request.getNotifyCustomUserIds()) : null)
                .notifyEmail(request.getNotifyEmail())
                .changePriority(request.getChangePriority())
                .addTags(request.getAddTags() != null ? String.join(",", request.getAddTags()) : null)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .executionOrder(request.getExecutionOrder() != null ? request.getExecutionOrder() : 0)
                .build();

        // Set SLA policy if provided
        if (request.getSlaPolicyId() != null) {
            slaPolicyRepository.findById(request.getSlaPolicyId())
                    .ifPresent(rule::setSlaPolicy);
        }

        EscalationRule saved = escalationService.createRule(rule);
        return ResponseEntity.ok(toEscalationResponse(saved));
    }

    @PutMapping("/escalation-rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EscalationRuleDto.Response> updateEscalationRule(
            @PathVariable UUID id,
            @Valid @RequestBody EscalationRuleDto.UpdateRequest request) {

        EscalationRule update = EscalationRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .triggerType(request.getTriggerType())
                .triggerMinutes(request.getTriggerMinutes())
                .escalationLevel(request.getEscalationLevel())
                .notifyAssignee(request.getNotifyAssignee())
                .notifyTeamLead(request.getNotifyTeamLead())
                .notifyManager(request.getNotifyManager())
                .notifyCustomUsers(request.getNotifyCustomUserIds() != null ?
                        String.join(",", request.getNotifyCustomUserIds()) : null)
                .notifyEmail(request.getNotifyEmail())
                .changePriority(request.getChangePriority())
                .addTags(request.getAddTags() != null ? String.join(",", request.getAddTags()) : null)
                .isActive(request.getIsActive())
                .executionOrder(request.getExecutionOrder())
                .build();

        EscalationRule updated = escalationService.updateRule(id, update);
        return ResponseEntity.ok(toEscalationResponse(updated));
    }

    @DeleteMapping("/escalation-rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEscalationRule(@PathVariable UUID id) {
        escalationService.deleteRule(id);
        return ResponseEntity.ok().build();
    }

    // ==================== Business Holidays ====================

    @GetMapping("/holidays")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<BusinessHoliday>> getHolidays(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) Integer year) {

        if (year == null) year = LocalDateTime.now().getYear();

        List<BusinessHoliday> holidays = businessHolidayRepository.findHolidaysInRange(
                projectId,
                java.time.LocalDate.of(year, 1, 1),
                java.time.LocalDate.of(year, 12, 31));

        return ResponseEntity.ok(holidays);
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessHoliday> createHoliday(@RequestBody BusinessHoliday holiday) {
        return ResponseEntity.ok(businessHolidayRepository.save(holiday));
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        businessHolidayRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== Helper Methods ====================

    private SlaPolicyDto.Response toResponse(SlaPolicy policy) {
        return SlaPolicyDto.Response.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .projectId(policy.getProjectId())
                .enabled(policy.getEnabled())
                .isDefault(policy.getIsDefault())
                .priorityOrder(policy.getPriorityOrder())
                .businessHoursOnly(policy.getBusinessHoursOnly())
                .businessHoursStart(policy.getBusinessHoursStart())
                .businessHoursEnd(policy.getBusinessHoursEnd())
                .businessDays(policy.getBusinessDays())
                .timezone(policy.getTimezone())
                .targets(policy.getTargets().stream()
                        .map(this::toTargetResponse)
                        .collect(Collectors.toList()))
                .conditions(policy.getConditions().stream()
                        .map(this::toConditionResponse)
                        .collect(Collectors.toList()))
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    private SlaPolicyDto.TargetResponse toTargetResponse(SlaTarget target) {
        return SlaPolicyDto.TargetResponse.builder()
                .id(target.getId())
                .targetType(target.getTargetType())
                .priority(target.getPriority())
                .targetMinutes(target.getTargetMinutes())
                .warningMinutes(target.getWarningMinutes())
                .targetFormatted(formatMinutes(target.getTargetMinutes()))
                .warningFormatted(formatMinutes(target.getWarningMinutes()))
                .build();
    }

    private SlaPolicyDto.ConditionResponse toConditionResponse(SlaCondition condition) {
        return SlaPolicyDto.ConditionResponse.builder()
                .id(condition.getId())
                .field(condition.getField())
                .operator(condition.getOperator())
                .value(condition.getValue())
                .build();
    }

    private EscalationRuleDto.Response toEscalationResponse(EscalationRule rule) {
        return EscalationRuleDto.Response.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .slaPolicyId(rule.getSlaPolicy() != null ? rule.getSlaPolicy().getId() : null)
                .slaPolicyName(rule.getSlaPolicy() != null ? rule.getSlaPolicy().getName() : null)
                .triggerType(rule.getTriggerType())
                .triggerMinutes(rule.getTriggerMinutes())
                .escalationLevel(rule.getEscalationLevel())
                .notifyAssignee(rule.getNotifyAssignee())
                .notifyTeamLead(rule.getNotifyTeamLead())
                .notifyManager(rule.getNotifyManager())
                .notifyCustomUserIds(rule.getNotifyCustomUsers() != null ?
                        Arrays.asList(rule.getNotifyCustomUsers().split(",")) : null)
                .notifyEmail(rule.getNotifyEmail())
                .reassignToUserId(rule.getReassignToUser() != null ? rule.getReassignToUser().getId() : null)
                .reassignToUserName(rule.getReassignToUser() != null ? rule.getReassignToUser().getEmail() : null)
                .reassignToTeamId(rule.getReassignToTeam() != null ? rule.getReassignToTeam().getId() : null)
                .reassignToTeamName(rule.getReassignToTeam() != null ? rule.getReassignToTeam().getName() : null)
                .changePriority(rule.getChangePriority())
                .addTags(rule.getAddTags() != null ? Arrays.asList(rule.getAddTags().split(",")) : null)
                .isActive(rule.getIsActive())
                .executionOrder(rule.getExecutionOrder())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private String formatMinutes(Integer minutes) {
        if (minutes == null) return null;
        if (minutes >= 60) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            return mins > 0 ? hours + "h " + mins + "m" : hours + "h";
        }
        return minutes + "m";
    }

    private Long calculateRemainingMinutes(LocalDateTime dueTime, LocalDateTime completedAt, LocalDateTime now) {
        if (dueTime == null) return null;
        if (completedAt != null) {
            return Duration.between(completedAt, dueTime).toMinutes();
        }
        return Duration.between(now, dueTime).toMinutes();
    }

    private String calculateSlaStatus(LocalDateTime dueTime, LocalDateTime completedAt,
                                      Boolean breached, LocalDateTime now) {
        if (Boolean.TRUE.equals(breached)) return "BREACHED";
        if (completedAt != null && dueTime != null) {
            return completedAt.isBefore(dueTime) || completedAt.isEqual(dueTime) ? "MET" : "BREACHED";
        }
        if (dueTime == null) return "N/A";

        long remainingMinutes = Duration.between(now, dueTime).toMinutes();
        if (remainingMinutes <= 0) return "BREACHED";
        if (remainingMinutes <= 30) return "AT_RISK";
        return "ON_TRACK";
    }

    private Double calculateComplianceRate(long withinSla, long breached) {
        long total = withinSla + breached;
        if (total == 0) return 100.0;
        return (double) withinSla / total * 100;
    }
}
