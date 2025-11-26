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
public class TicketDto {

    private UUID id;
    private String ticketNumber;
    private String subject;
    private String description;
    private Ticket.TicketStatus status;
    private Ticket.TicketPriority priority;
    private Ticket.TicketType type;
    private Ticket.TicketChannel channel;

    private UUID projectId;
    private String projectName;
    private String projectKey;

    private UUID categoryId;
    private String categoryName;

    private UUID requesterId;
    private String requesterName;
    private String requesterEmail;
    private String requesterAvatarUrl;

    private UUID assigneeId;
    private String assigneeName;
    private String assigneeEmail;
    private String assigneeAvatarUrl;

    private UUID teamId;
    private String teamName;

    private LocalDateTime dueDate;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private Integer reopenedCount;

    private Integer csatRating;
    private String csatComment;

    private Set<String> tags;

    private Integer commentCount;
    private Integer attachmentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
