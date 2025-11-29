package com.servicedesk.monolith.ticket.service;
\nimport org.springframework.context.ApplicationEventPublisher;

import com.servicedesk.monolith.ticket.dto.EscalationRuleDto;
import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EscalationService {

    private final EscalationRuleRepository escalationRuleRepository;
    private final TicketRepository ticketRepository;
    private final SlaBreachHistoryRepository slaBreachHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void processEscalations() {
        log.debug("Processing escalations...");

        LocalDateTime now = LocalDateTime.now();

        // Process SLA warnings (15 minutes before breach)
        processWarningEscalations(now);

        // Process SLA breaches
        processBreachEscalations(now);

        // Process time-based escalations
        processTimeBasedEscalations(now);
    }

    private void processWarningEscalations(LocalDateTime now) {
        List<EscalationRule> warningRules = escalationRuleRepository
                .findByTriggerType(EscalationRule.TriggerType.SLA_WARNING);

        for (EscalationRule rule : warningRules) {
            int warningMinutes = rule.getTriggerMinutes() != null ? rule.getTriggerMinutes() : 15;
            LocalDateTime warningThreshold = now.plusMinutes(warningMinutes);

            // First response warnings
            List<Ticket> firstResponseAtRisk = ticketRepository
                    .findTicketsWithFirstResponseDueBefore(warningThreshold);
            for (Ticket ticket : firstResponseAtRisk) {
                if (ticket.getFirstResponseAt() == null && !Boolean.TRUE.equals(ticket.getFirstResponseBreached())) {
                    executeEscalation(ticket, rule, "FIRST_RESPONSE_WARNING");
                }
            }

            // Resolution warnings
            List<Ticket> resolutionAtRisk = ticketRepository
                    .findTicketsWithResolutionDueBefore(warningThreshold);
            for (Ticket ticket : resolutionAtRisk) {
                if (ticket.getResolvedAt() == null && !Boolean.TRUE.equals(ticket.getResolutionBreached())) {
                    executeEscalation(ticket, rule, "RESOLUTION_WARNING");
                }
            }
        }
    }

    private void processBreachEscalations(LocalDateTime now) {
        List<EscalationRule> breachRules = escalationRuleRepository
                .findByTriggerType(EscalationRule.TriggerType.SLA_BREACH);

        List<Ticket> breachedTickets = ticketRepository.findTicketsWithSlaBreached(now);

        for (Ticket ticket : breachedTickets) {
            boolean wasBreached = Boolean.TRUE.equals(ticket.getSlaBreached());

            // Check first response breach
            if (ticket.getFirstResponseDue() != null &&
                ticket.getFirstResponseAt() == null &&
                ticket.getFirstResponseDue().isBefore(now) &&
                !Boolean.TRUE.equals(ticket.getFirstResponseBreached())) {

                ticket.setFirstResponseBreached(true);
                ticket.setSlaBreached(true);
                recordBreach(ticket, SlaBreachHistory.BreachType.FIRST_RESPONSE, ticket.getFirstResponseDue(), now);
            }

            // Check resolution breach
            if (ticket.getResolutionDue() != null &&
                ticket.getResolvedAt() == null &&
                ticket.getResolutionDue().isBefore(now) &&
                !Boolean.TRUE.equals(ticket.getResolutionBreached())) {

                ticket.setResolutionBreached(true);
                ticket.setSlaBreached(true);
                recordBreach(ticket, SlaBreachHistory.BreachType.RESOLUTION, ticket.getResolutionDue(), now);
            }

            if (Boolean.TRUE.equals(ticket.getSlaBreached()) && !wasBreached) {
                ticketRepository.save(ticket);

                for (EscalationRule rule : breachRules) {
                    executeEscalation(ticket, rule, "SLA_BREACH");
                }
            }
        }
    }

    private void processTimeBasedEscalations(LocalDateTime now) {
        List<EscalationRule> timeBasedRules = escalationRuleRepository
                .findByTriggerType(EscalationRule.TriggerType.TIME_BASED);

        for (EscalationRule rule : timeBasedRules) {
            if (rule.getTriggerMinutes() == null) continue;

            LocalDateTime threshold = now.minusMinutes(rule.getTriggerMinutes());
            List<Ticket> staleTickets = ticketRepository.findStaleTickets(threshold);

            for (Ticket ticket : staleTickets) {
                executeEscalation(ticket, rule, "TIME_BASED");
            }
        }
    }

    private void recordBreach(Ticket ticket, SlaBreachHistory.BreachType type,
                             LocalDateTime dueAt, LocalDateTime breachedAt) {
        int breachDuration = (int) Duration.between(dueAt, breachedAt).toMinutes();

        SlaBreachHistory breach = SlaBreachHistory.builder()
                .ticket(ticket)
                .slaPolicy(ticket.getSlaPolicy())
                .breachType(type)
                .dueAt(dueAt)
                .breachedAt(breachedAt)
                .breachDurationMinutes(breachDuration)
                .escalationTriggered(true)
                .build();

        slaBreachHistoryRepository.save(breach);
        log.warn("SLA breach recorded for ticket {}: {} - {} minutes overdue",
                ticket.getTicketNumber(), type, breachDuration);
    }

    @Transactional
    public void executeEscalation(Ticket ticket, EscalationRule rule, String reason) {
        log.info("Executing escalation rule '{}' for ticket {} ({})",
                rule.getName(), ticket.getTicketNumber(), reason);

        List<String> notifiedUsers = new ArrayList<>();

        // Notify assignee
        if (Boolean.TRUE.equals(rule.getNotifyAssignee()) && ticket.getAssignee() != null) {
            notifiedUsers.add(ticket.getAssignee().getId());
            sendNotification(ticket.getAssignee().getId(), ticket, rule, reason);
        }

        // Notify team lead
        if (Boolean.TRUE.equals(rule.getNotifyTeamLead()) && ticket.getTeam() != null) {
            Team team = ticket.getTeam();
            if (team.getManager() != null) {
                notifiedUsers.add(team.getManager().getId());
                sendNotification(team.getManager().getId(), ticket, rule, reason);
            }
        }

        // Notify custom users
        if (rule.getNotifyCustomUsers() != null && !rule.getNotifyCustomUsers().isEmpty()) {
            for (String userId : rule.getNotifyCustomUsers().split(",")) {
                userId = userId.trim();
                if (!userId.isEmpty()) {
                    notifiedUsers.add(userId);
                    sendNotification(userId, ticket, rule, reason);
                }
            }
        }

        // Send external email
        if (rule.getNotifyEmail() != null && !rule.getNotifyEmail().isEmpty()) {
            sendExternalEmail(rule.getNotifyEmail(), ticket, rule, reason);
        }

        // Reassign ticket
        if (rule.getReassignToUser() != null) {
            ticket.setAssignee(rule.getReassignToUser());
            addTicketHistory(ticket, "REASSIGNED",
                    "Escalation: Reassigned to " + rule.getReassignToUser().getEmail());
        }

        if (rule.getReassignToTeam() != null) {
            ticket.setTeam(rule.getReassignToTeam());
            addTicketHistory(ticket, "TEAM_CHANGED",
                    "Escalation: Team changed to " + rule.getReassignToTeam().getName());
        }

        // Change priority
        if (rule.getChangePriority() != null) {
            Ticket.TicketPriority oldPriority = ticket.getPriority();
            ticket.setPriority(rule.getChangePriority());
            addTicketHistory(ticket, "PRIORITY_CHANGED",
                    "Escalation: Priority changed from " + oldPriority + " to " + rule.getChangePriority());
        }

        // Add tags
        if (rule.getAddTags() != null && !rule.getAddTags().isEmpty()) {
            Set<String> tags = ticket.getTags();
            for (String tag : rule.getAddTags().split(",")) {
                tags.add(tag.trim());
            }
        }

        ticketRepository.save(ticket);

        // Publish escalation event
        publishEscalationEvent(ticket, rule, reason, notifiedUsers);
    }

    private void sendNotification(String userId, Ticket ticket, EscalationRule rule, String reason) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ESCALATION");
        notification.put("userId", userId);
        notification.put("ticketId", ticket.getId());
        notification.put("ticketNumber", ticket.getTicketNumber());
        notification.put("ticketSubject", ticket.getSubject());
        notification.put("ruleName", rule.getName());
        notification.put("reason", reason);
        notification.put("escalationLevel", rule.getEscalationLevel());
        notification.put("timestamp", LocalDateTime.now().toString());

        eventPublisher.convertAndSend("notification.queue", notification);
    }

    private void sendExternalEmail(String email, Ticket ticket, EscalationRule rule, String reason) {
        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("type", "ESCALATION_EMAIL");
        emailRequest.put("to", email);
        emailRequest.put("ticketId", ticket.getId());
        emailRequest.put("ticketNumber", ticket.getTicketNumber());
        emailRequest.put("ticketSubject", ticket.getSubject());
        emailRequest.put("ruleName", rule.getName());
        emailRequest.put("reason", reason);
        emailRequest.put("escalationLevel", rule.getEscalationLevel());

        eventPublisher.convertAndSend("email.queue", emailRequest);
    }

    private void addTicketHistory(Ticket ticket, String action, String description) {
        TicketHistory history = TicketHistory.builder()
                .action(action)
                .description(description)
                .build();
        ticket.addHistoryEntry(history);
    }

    private void publishEscalationEvent(Ticket ticket, EscalationRule rule, String reason, List<String> notifiedUsers) {
        EscalationRuleDto.EscalationEvent event = EscalationRuleDto.EscalationEvent.builder()
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .triggerType(rule.getTriggerType())
                .escalationLevel(rule.getEscalationLevel())
                .notifiedUsers(notifiedUsers)
                .newAssigneeId(rule.getReassignToUser() != null ? rule.getReassignToUser().getId() : null)
                .newTeamId(rule.getReassignToTeam() != null ? rule.getReassignToTeam().getId() : null)
                .newPriority(rule.getChangePriority())
                .escalatedAt(LocalDateTime.now())
                .build();

        eventPublisher.convertAndSend("escalation.events", event);
    }

    // CRUD operations for escalation rules
    public EscalationRule createRule(EscalationRule rule) {
        return escalationRuleRepository.save(rule);
    }

    public Optional<EscalationRule> getRule(UUID id) {
        return escalationRuleRepository.findById(id);
    }

    public List<EscalationRule> getAllActiveRules() {
        return escalationRuleRepository.findAllActiveRules();
    }

    public List<EscalationRule> getRulesByPolicy(UUID policyId) {
        return escalationRuleRepository.findBySlaPolicyId(policyId);
    }

    public EscalationRule updateRule(UUID id, EscalationRule updated) {
        return escalationRuleRepository.findById(id)
                .map(existing -> {
                    if (updated.getName() != null) existing.setName(updated.getName());
                    if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
                    if (updated.getTriggerType() != null) existing.setTriggerType(updated.getTriggerType());
                    if (updated.getTriggerMinutes() != null) existing.setTriggerMinutes(updated.getTriggerMinutes());
                    if (updated.getEscalationLevel() != null) existing.setEscalationLevel(updated.getEscalationLevel());
                    if (updated.getNotifyAssignee() != null) existing.setNotifyAssignee(updated.getNotifyAssignee());
                    if (updated.getNotifyTeamLead() != null) existing.setNotifyTeamLead(updated.getNotifyTeamLead());
                    if (updated.getNotifyManager() != null) existing.setNotifyManager(updated.getNotifyManager());
                    if (updated.getNotifyCustomUsers() != null) existing.setNotifyCustomUsers(updated.getNotifyCustomUsers());
                    if (updated.getNotifyEmail() != null) existing.setNotifyEmail(updated.getNotifyEmail());
                    if (updated.getChangePriority() != null) existing.setChangePriority(updated.getChangePriority());
                    if (updated.getAddTags() != null) existing.setAddTags(updated.getAddTags());
                    if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());
                    if (updated.getExecutionOrder() != null) existing.setExecutionOrder(updated.getExecutionOrder());
                    return escalationRuleRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Escalation rule not found: " + id));
    }

    public void deleteRule(UUID id) {
        escalationRuleRepository.findById(id).ifPresent(rule -> {
            rule.setDeleted(true);
            escalationRuleRepository.save(rule);
        });
    }
}
