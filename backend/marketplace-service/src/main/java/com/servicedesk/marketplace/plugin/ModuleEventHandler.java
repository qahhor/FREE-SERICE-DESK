package com.servicedesk.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

/**
 * Represents an event handler registered by a module
 */
@Data
@Builder
public class ModuleEventHandler {

    /**
     * Event type to listen for
     */
    private String eventType;

    /**
     * Handler function
     */
    private Consumer<ModuleEvent> handler;

    /**
     * Priority (lower = higher priority)
     */
    @Builder.Default
    private int priority = 100;

    /**
     * Whether handler runs async
     */
    @Builder.Default
    private boolean async = false;

    /**
     * Common event types
     */
    public static final String TICKET_CREATED = "ticket.created";
    public static final String TICKET_UPDATED = "ticket.updated";
    public static final String TICKET_ASSIGNED = "ticket.assigned";
    public static final String TICKET_RESOLVED = "ticket.resolved";
    public static final String TICKET_CLOSED = "ticket.closed";
    public static final String COMMENT_ADDED = "comment.added";
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String SLA_WARNING = "sla.warning";
    public static final String SLA_BREACHED = "sla.breached";
    public static final String CUSTOMER_FEEDBACK = "customer.feedback";
}
