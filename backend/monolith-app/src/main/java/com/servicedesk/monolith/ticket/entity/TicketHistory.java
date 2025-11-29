package com.servicedesk.monolith.ticket.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private HistoryAction action;

    @Column(name = "field_name", length = 50)
    private String fieldName;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "description")
    private String description;

    public enum HistoryAction {
        CREATED,
        UPDATED,
        STATUS_CHANGED,
        PRIORITY_CHANGED,
        ASSIGNED,
        UNASSIGNED,
        TEAM_CHANGED,
        CATEGORY_CHANGED,
        COMMENT_ADDED,
        ATTACHMENT_ADDED,
        ATTACHMENT_REMOVED,
        TAG_ADDED,
        TAG_REMOVED,
        WATCHER_ADDED,
        WATCHER_REMOVED,
        MERGED,
        SPLIT,
        LINKED,
        UNLINKED,
        SLA_BREACHED,
        ESCALATED
    }
}
