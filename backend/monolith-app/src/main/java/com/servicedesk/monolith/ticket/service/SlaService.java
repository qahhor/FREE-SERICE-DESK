package com.servicedesk.monolith.ticket.service;

import org.springframework.context.ApplicationEventPublisher;

import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.repository.SlaPolicyRepository;
import com.servicedesk.monolith.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SlaPolicy findApplicablePolicy(Ticket ticket) {
        List<SlaPolicy> policies = slaPolicyRepository.findByProjectIdAndEnabledTrueOrderByPriorityOrderAsc(
                ticket.getProject() != null ? ticket.getProject().getId() : null);

        for (SlaPolicy policy : policies) {
            if (matchesConditions(ticket, policy.getConditions())) {
                return policy;
            }
        }

        // Return default policy if no specific match
        return slaPolicyRepository.findByIsDefaultTrueAndEnabledTrue().orElse(null);
    }

    public void applySlaTotTicket(Ticket ticket) {
        SlaPolicy policy = findApplicablePolicy(ticket);
        if (policy == null) {
            return;
        }

        for (SlaTarget target : policy.getTargets()) {
            if (target.getPriority() == ticket.getPriority()) {
                LocalDateTime dueTime = calculateDueTime(
                        ticket.getCreatedAt(),
                        target.getTargetMinutes(),
                        policy
                );

                switch (target.getTargetType()) {
                    case FIRST_RESPONSE -> ticket.setFirstResponseDue(dueTime);
                    case RESOLUTION -> ticket.setResolutionDue(dueTime);
                }

                if (target.getWarningMinutes() != null) {
                    LocalDateTime warningTime = calculateDueTime(
                            ticket.getCreatedAt(),
                            target.getWarningMinutes(),
                            policy
                    );
                    // Store warning time if needed
                }
            }
        }

        ticketRepository.save(ticket);
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        // Check first response SLA
        List<Ticket> firstResponseAtRisk = ticketRepository.findTicketsWithFirstResponseDueBefore(
                now.plusMinutes(15));
        for (Ticket ticket : firstResponseAtRisk) {
            if (ticket.getFirstResponseAt() == null) {
                sendSlaWarning(ticket, SlaTarget.TargetType.FIRST_RESPONSE);
            }
        }

        // Check resolution SLA
        List<Ticket> resolutionAtRisk = ticketRepository.findTicketsWithResolutionDueBefore(
                now.plusMinutes(30));
        for (Ticket ticket : resolutionAtRisk) {
            if (ticket.getResolvedAt() == null) {
                sendSlaWarning(ticket, SlaTarget.TargetType.RESOLUTION);
            }
        }

        // Mark breached tickets
        List<Ticket> breached = ticketRepository.findTicketsWithSlaBreached(now);
        for (Ticket ticket : breached) {
            if (!Boolean.TRUE.equals(ticket.getSlaBreached())) {
                ticket.setSlaBreached(true);
                ticketRepository.save(ticket);
                sendSlaBreachNotification(ticket);
            }
        }
    }

    private boolean matchesConditions(Ticket ticket, List<SlaCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (SlaCondition condition : conditions) {
            if (!evaluateCondition(ticket, condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateCondition(Ticket ticket, SlaCondition condition) {
        String fieldValue = getFieldValue(ticket, condition.getField());
        String conditionValue = condition.getValue();

        return switch (condition.getOperator()) {
            case EQUALS -> Objects.equals(fieldValue, conditionValue);
            case NOT_EQUALS -> !Objects.equals(fieldValue, conditionValue);
            case CONTAINS -> fieldValue != null && fieldValue.contains(conditionValue);
            case NOT_CONTAINS -> fieldValue == null || !fieldValue.contains(conditionValue);
            case STARTS_WITH -> fieldValue != null && fieldValue.startsWith(conditionValue);
            case ENDS_WITH -> fieldValue != null && fieldValue.endsWith(conditionValue);
            case IN -> Arrays.asList(conditionValue.split(",")).contains(fieldValue);
            case NOT_IN -> !Arrays.asList(conditionValue.split(",")).contains(fieldValue);
            case IS_EMPTY -> fieldValue == null || fieldValue.isEmpty();
            case IS_NOT_EMPTY -> fieldValue != null && !fieldValue.isEmpty();
            default -> false;
        };
    }

    private String getFieldValue(Ticket ticket, String field) {
        return switch (field.toLowerCase()) {
            case "priority" -> ticket.getPriority() != null ? ticket.getPriority().name() : null;
            case "status" -> ticket.getStatus() != null ? ticket.getStatus().name() : null;
            case "type" -> ticket.getType() != null ? ticket.getType().name() : null;
            case "channel" -> ticket.getChannel() != null ? ticket.getChannel().name() : null;
            case "category" -> ticket.getCategory() != null ? ticket.getCategory().getId() : null;
            case "team" -> ticket.getTeam() != null ? ticket.getTeam().getId() : null;
            case "assignee" -> ticket.getAssignee() != null ? ticket.getAssignee().getId() : null;
            case "subject" -> ticket.getSubject();
            default -> null;
        };
    }

    private LocalDateTime calculateDueTime(LocalDateTime startTime, int minutes, SlaPolicy policy) {
        if (!Boolean.TRUE.equals(policy.getBusinessHoursOnly())) {
            return startTime.plusMinutes(minutes);
        }

        // Calculate with business hours
        ZoneId zoneId = ZoneId.of(policy.getTimezone());
        ZonedDateTime current = startTime.atZone(zoneId);
        int remainingMinutes = minutes;

        LocalTime businessStart = LocalTime.parse(policy.getBusinessHoursStart());
        LocalTime businessEnd = LocalTime.parse(policy.getBusinessHoursEnd());
        Set<DayOfWeek> businessDays = parseBusinessDays(policy.getBusinessDays());

        while (remainingMinutes > 0) {
            if (isBusinessTime(current, businessStart, businessEnd, businessDays)) {
                LocalTime currentTime = current.toLocalTime();
                int minutesUntilEnd = (int) Duration.between(currentTime, businessEnd).toMinutes();

                if (remainingMinutes <= minutesUntilEnd) {
                    current = current.plusMinutes(remainingMinutes);
                    remainingMinutes = 0;
                } else {
                    remainingMinutes -= minutesUntilEnd;
                    current = current.with(businessEnd).plusDays(1).with(businessStart);
                }
            } else {
                current = nextBusinessStart(current, businessStart, businessDays);
            }
        }

        return current.toLocalDateTime();
    }

    private boolean isBusinessTime(ZonedDateTime time, LocalTime start, LocalTime end, Set<DayOfWeek> days) {
        if (!days.contains(time.getDayOfWeek())) {
            return false;
        }
        LocalTime currentTime = time.toLocalTime();
        return !currentTime.isBefore(start) && currentTime.isBefore(end);
    }

    private ZonedDateTime nextBusinessStart(ZonedDateTime current, LocalTime businessStart, Set<DayOfWeek> businessDays) {
        ZonedDateTime next = current.with(businessStart);
        if (next.isBefore(current) || next.equals(current)) {
            next = next.plusDays(1);
        }
        while (!businessDays.contains(next.getDayOfWeek())) {
            next = next.plusDays(1);
        }
        return next;
    }

    private Set<DayOfWeek> parseBusinessDays(String businessDays) {
        Set<DayOfWeek> days = new HashSet<>();
        for (String day : businessDays.split(",")) {
            days.add(DayOfWeek.of(Integer.parseInt(day.trim())));
        }
        return days;
    }

    private void sendSlaWarning(Ticket ticket, SlaTarget.TargetType targetType) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "SLA_WARNING");
        event.put("ticketId", ticket.getId());
        event.put("ticketNumber", ticket.getTicketNumber());
        event.put("targetType", targetType.name());
        event.put("assigneeId", ticket.getAssignee() != null ? ticket.getAssignee().getId() : null);

        eventPublisher.convertAndSend("notification.queue", event);
        log.warn("SLA warning for ticket {}: {}", ticket.getTicketNumber(), targetType);
    }

    private void sendSlaBreachNotification(Ticket ticket) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "SLA_BREACHED");
        event.put("ticketId", ticket.getId());
        event.put("ticketNumber", ticket.getTicketNumber());
        event.put("assigneeId", ticket.getAssignee() != null ? ticket.getAssignee().getId() : null);
        event.put("teamId", ticket.getTeam() != null ? ticket.getTeam().getId() : null);

        eventPublisher.convertAndSend("notification.queue", event);
        log.error("SLA breached for ticket {}", ticket.getTicketNumber());
    }
}
