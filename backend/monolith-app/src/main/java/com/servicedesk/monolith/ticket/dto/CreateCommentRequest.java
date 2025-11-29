package com.servicedesk.monolith.ticket.dto;

import com.servicedesk.monolith.ticket.entity.TicketComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private String content;

    private String contentHtml;

    @Builder.Default
    private TicketComment.CommentType type = TicketComment.CommentType.PUBLIC;
}
