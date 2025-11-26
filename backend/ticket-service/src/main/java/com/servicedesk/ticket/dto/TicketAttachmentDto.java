package com.servicedesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAttachmentDto {

    private UUID id;
    private UUID ticketId;
    private UUID commentId;
    private UUID uploadedById;
    private String uploadedByName;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
}
