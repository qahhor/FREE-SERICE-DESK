package com.servicedesk.monolith.ticket.dto;

import com.servicedesk.monolith.ticket.entity.Ticket;
import jakarta.validation.constraints.Size;
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
public class UpdateTicketRequest {

    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @Size(max = 50000, message = "Description must not exceed 50000 characters")
    private String description;

    private Ticket.TicketStatus status;

    private Ticket.TicketPriority priority;

    private Ticket.TicketType type;

    private UUID categoryId;

    private UUID assigneeId;

    private UUID teamId;

    private LocalDateTime dueDate;

    private Set<String> tags;

    private String metadata;
}
