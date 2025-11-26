package com.servicedesk.ticket.dto;

import com.servicedesk.ticket.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateTicketRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @Size(max = 50000, message = "Description must not exceed 50000 characters")
    private String description;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID categoryId;

    private UUID assigneeId;

    private UUID teamId;

    private Ticket.TicketPriority priority;

    private Ticket.TicketType type;

    private Ticket.TicketChannel channel;

    private LocalDateTime dueDate;

    private Set<String> tags;

    private String externalId;

    private String metadata;
}
