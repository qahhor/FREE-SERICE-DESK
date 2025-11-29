package com.servicedesk.monolith.ticket.dto;

import com.servicedesk.monolith.ticket.entity.Ticket;
import com.servicedesk.monolith.ticket.entity.TicketComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentDto {

    private UUID id;
    private UUID ticketId;
    private UUID authorId;
    private String authorName;
    private String authorEmail;
    private String authorAvatarUrl;
    private String content;
    private String contentHtml;
    private TicketComment.CommentType type;
    private Ticket.TicketChannel channel;
    private List<TicketAttachmentDto> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
