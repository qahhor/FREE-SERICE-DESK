package com.servicedesk.monolith.ticket.service;
\nimport org.springframework.context.ApplicationEventPublisher;

import com.servicedesk.monolith.ticket.entity.*;
import com.servicedesk.monolith.ticket.repository.AutomationRuleRepository;
import com.servicedesk.monolith.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutomationEngine {

    private final AutomationRuleRepository ruleRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processTicketEvent(String ticketId, AutomationRule.TriggerEvent event, Map<String, Object> context) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            log.warn("Ticket not found for automation: {}", ticketId);
            return;
        }

        String projectId = ticket.getProject() != null ? ticket.getProject().getId() : null;

        List<AutomationRule> rules = ruleRepository.findByTriggerEventAndEnabledTrueOrderByExecutionOrderAsc(event);

        for (AutomationRule rule : rules) {
            // Check project scope
            if (rule.getProjectId() != null && !rule.getProjectId().equals(projectId)) {
                continue;
            }

            try {
                if (evaluateConditions(ticket, rule.getConditions(), context)) {
                    executeActions(ticket, rule.getActions(), context);

                    // Update execution count
                    rule.setExecutionCount(rule.getExecutionCount() + 1);
                    ruleRepository.save(rule);

                    log.info("Automation rule '{}' executed for ticket {}", rule.getName(), ticket.getTicketNumber());

                    if (Boolean.TRUE.equals(rule.getStopProcessing())) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error executing automation rule '{}': {}", rule.getName(), e.getMessage(), e);
            }
        }
    }

    private boolean evaluateConditions(Ticket ticket, List<AutomationCondition> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        Boolean result = null;
        AutomationCondition.LogicOperator lastOperator = AutomationCondition.LogicOperator.AND;

        for (AutomationCondition condition : conditions) {
            boolean conditionResult = evaluateCondition(ticket, condition, context);

            if (result == null) {
                result = conditionResult;
            } else {
                result = switch (lastOperator) {
                    case AND -> result && conditionResult;
                    case OR -> result || conditionResult;
                };
            }

            lastOperator = condition.getLogicOperator();
        }

        return result != null && result;
    }

    private boolean evaluateCondition(Ticket ticket, AutomationCondition condition, Map<String, Object> context) {
        String fieldValue = getFieldValue(ticket, condition.getField(), context);
        String conditionValue = condition.getValue();

        return switch (condition.getOperator()) {
            case EQUALS -> Objects.equals(fieldValue, conditionValue);
            case NOT_EQUALS -> !Objects.equals(fieldValue, conditionValue);
            case CONTAINS -> fieldValue != null && fieldValue.toLowerCase().contains(conditionValue.toLowerCase());
            case NOT_CONTAINS -> fieldValue == null || !fieldValue.toLowerCase().contains(conditionValue.toLowerCase());
            case STARTS_WITH -> fieldValue != null && fieldValue.toLowerCase().startsWith(conditionValue.toLowerCase());
            case ENDS_WITH -> fieldValue != null && fieldValue.toLowerCase().endsWith(conditionValue.toLowerCase());
            case IN -> Arrays.asList(conditionValue.split(",")).contains(fieldValue);
            case NOT_IN -> !Arrays.asList(conditionValue.split(",")).contains(fieldValue);
            case GREATER_THAN -> compareNumbers(fieldValue, conditionValue) > 0;
            case LESS_THAN -> compareNumbers(fieldValue, conditionValue) < 0;
            case IS_EMPTY -> fieldValue == null || fieldValue.isEmpty();
            case IS_NOT_EMPTY -> fieldValue != null && !fieldValue.isEmpty();
        };
    }

    private int compareNumbers(String value1, String value2) {
        try {
            return Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getFieldValue(Ticket ticket, String field, Map<String, Object> context) {
        // First check context for computed values
        if (context != null && context.containsKey(field)) {
            Object value = context.get(field);
            return value != null ? value.toString() : null;
        }

        // Then check ticket fields
        return switch (field.toLowerCase()) {
            case "priority" -> ticket.getPriority() != null ? ticket.getPriority().name() : null;
            case "status" -> ticket.getStatus() != null ? ticket.getStatus().name() : null;
            case "type" -> ticket.getType() != null ? ticket.getType().name() : null;
            case "channel" -> ticket.getChannel() != null ? ticket.getChannel().name() : null;
            case "category" -> ticket.getCategory() != null ? ticket.getCategory().getName() : null;
            case "category_id" -> ticket.getCategory() != null ? ticket.getCategory().getId() : null;
            case "team" -> ticket.getTeam() != null ? ticket.getTeam().getName() : null;
            case "team_id" -> ticket.getTeam() != null ? ticket.getTeam().getId() : null;
            case "assignee_id" -> ticket.getAssignee() != null ? ticket.getAssignee().getId() : null;
            case "subject" -> ticket.getSubject();
            case "description" -> ticket.getDescription();
            case "requester_email" -> ticket.getRequester() != null ? ticket.getRequester().getEmail() : null;
            case "tags" -> ticket.getTags() != null ? String.join(",", ticket.getTags()) : null;
            default -> null;
        };
    }

    @Transactional
    private void executeActions(Ticket ticket, List<AutomationAction> actions, Map<String, Object> context) {
        for (AutomationAction action : actions) {
            try {
                executeAction(ticket, action, context);
            } catch (Exception e) {
                log.error("Failed to execute action {}: {}", action.getActionType(), e.getMessage());
            }
        }
        ticketRepository.save(ticket);
    }

    private void executeAction(Ticket ticket, AutomationAction action, Map<String, Object> context) {
        switch (action.getActionType()) {
            case SET_STATUS -> {
                Ticket.TicketStatus newStatus = Ticket.TicketStatus.valueOf(action.getTargetValue());
                ticket.setStatus(newStatus);
                if (newStatus == Ticket.TicketStatus.RESOLVED) {
                    ticket.setResolvedAt(LocalDateTime.now());
                }
            }
            case SET_PRIORITY -> {
                ticket.setPriority(Ticket.TicketPriority.valueOf(action.getTargetValue()));
            }
            case SET_TYPE -> {
                ticket.setType(Ticket.TicketType.valueOf(action.getTargetValue()));
            }
            case ADD_TAG -> {
                if (ticket.getTags() == null) {
                    ticket.setTags(new HashSet<>());
                }
                ticket.getTags().add(action.getTargetValue());
            }
            case REMOVE_TAG -> {
                if (ticket.getTags() != null) {
                    ticket.getTags().remove(action.getTargetValue());
                }
            }
            case SEND_EMAIL -> sendEmailAction(ticket, action);
            case SEND_NOTIFICATION -> sendNotificationAction(ticket, action);
            case SEND_WEBHOOK -> sendWebhookAction(ticket, action);
            case ADD_INTERNAL_NOTE -> addInternalNote(ticket, action);
            case ESCALATE -> escalateTicket(ticket, action);
            default -> log.warn("Unhandled action type: {}", action.getActionType());
        }
    }

    private void sendEmailAction(Ticket ticket, AutomationAction action) {
        Map<String, Object> emailEvent = new HashMap<>();
        emailEvent.put("type", "AUTOMATION_EMAIL");
        emailEvent.put("ticketId", ticket.getId());
        emailEvent.put("template", action.getTargetField());
        emailEvent.put("recipients", action.getTargetValue());

        eventPublisher.convertAndSend("email.queue", emailEvent);
    }

    private void sendNotificationAction(Ticket ticket, AutomationAction action) {
        Map<String, Object> notificationEvent = new HashMap<>();
        notificationEvent.put("type", "AUTOMATION_NOTIFICATION");
        notificationEvent.put("ticketId", ticket.getId());
        notificationEvent.put("message", action.getTargetValue());
        notificationEvent.put("recipients", action.getTargetField());

        eventPublisher.convertAndSend("notification.queue", notificationEvent);
    }

    private void sendWebhookAction(Ticket ticket, AutomationAction action) {
        Map<String, Object> webhookEvent = new HashMap<>();
        webhookEvent.put("type", "WEBHOOK");
        webhookEvent.put("url", action.getTargetValue());
        webhookEvent.put("ticketId", ticket.getId());
        webhookEvent.put("ticketNumber", ticket.getTicketNumber());
        webhookEvent.put("subject", ticket.getSubject());
        webhookEvent.put("status", ticket.getStatus().name());

        eventPublisher.convertAndSend("webhook.queue", webhookEvent);
    }

    private void addInternalNote(Ticket ticket, AutomationAction action) {
        // This would typically create a TicketComment with internal flag
        log.info("Adding internal note to ticket {}: {}", ticket.getTicketNumber(), action.getTargetValue());
    }

    private void escalateTicket(Ticket ticket, AutomationAction action) {
        ticket.setEscalated(true);
        ticket.setEscalatedAt(LocalDateTime.now());

        Map<String, Object> escalationEvent = new HashMap<>();
        escalationEvent.put("type", "TICKET_ESCALATED");
        escalationEvent.put("ticketId", ticket.getId());
        escalationEvent.put("ticketNumber", ticket.getTicketNumber());

        eventPublisher.convertAndSend("notification.queue", escalationEvent);
    }

    public void handleAutomationEvent(Map<String, Object> event) {
        String ticketId = (String) event.get("ticketId");
        String eventType = (String) event.get("eventType");

        if (ticketId != null && eventType != null) {
            try {
                AutomationRule.TriggerEvent trigger = AutomationRule.TriggerEvent.valueOf(eventType);
                processTicketEvent(ticketId, trigger, event);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown automation event type: {}", eventType);
            }
        }
    }
}
