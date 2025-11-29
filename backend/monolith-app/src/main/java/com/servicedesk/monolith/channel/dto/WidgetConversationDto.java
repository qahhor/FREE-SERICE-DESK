package com.servicedesk.monolith.channel.dto;

import com.servicedesk.monolith.channel.entity.WidgetConversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetConversationDto {
    private String id;
    private String projectId;
    private String visitorId;
    private String visitorName;
    private String visitorEmail;
    private String ticketId;
    private String assignedAgentId;
    private WidgetConversation.ConversationStatus status;
    private String pageUrl;
    private String pageTitle;
    private String locale;
    private LocalDateTime lastMessageAt;
    private Integer rating;
    private List<WidgetMessageDto> messages;
    private LocalDateTime createdAt;
}
