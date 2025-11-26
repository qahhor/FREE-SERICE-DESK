package com.servicedesk.notification.dto;

import com.servicedesk.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String id;
    private String userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private String entityType;
    private String entityId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Notification.Priority priority;
    private LocalDateTime createdAt;
}
