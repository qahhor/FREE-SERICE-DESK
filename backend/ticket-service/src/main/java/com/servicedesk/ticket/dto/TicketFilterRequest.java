package com.servicedesk.ticket.dto;

import com.servicedesk.ticket.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilterRequest {

    private String search;

    private Set<Ticket.TicketStatus> statuses;

    private Set<Ticket.TicketPriority> priorities;

    private Set<Ticket.TicketType> types;

    private Set<Ticket.TicketChannel> channels;

    private Set<UUID> projectIds;

    private Set<UUID> categoryIds;

    private Set<UUID> assigneeIds;

    private Set<UUID> requesterIds;

    private Set<UUID> teamIds;

    private Set<String> tags;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;

    private LocalDateTime dueDateFrom;

    private LocalDateTime dueDateTo;

    private Boolean unassigned;

    private Boolean overdue;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
