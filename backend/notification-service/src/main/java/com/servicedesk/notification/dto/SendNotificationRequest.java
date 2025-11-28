package com.servicedesk.notification.dto;

import com.servicedesk.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    private String userId;
    private String userEmail;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private String entityType;
    private String entityId;
    private Notification.Priority priority;
    private Map<String, Object> metadata;
}
