package com.servicedesk.channel.dto;

import com.servicedesk.channel.entity.WidgetMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetMessageDto {
    private String id;
    private String conversationId;
    private WidgetMessage.SenderType senderType;
    private String senderId;
    private String senderName;
    private String content;
    private WidgetMessage.MessageType messageType;
    private String attachmentUrl;
    private String attachmentName;
    private Boolean isRead;
    private LocalDateTime timestamp;
}
