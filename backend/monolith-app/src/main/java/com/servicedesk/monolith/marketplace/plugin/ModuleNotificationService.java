package com.servicedesk.monolith.marketplace.plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Notification service interface for modules to send notifications
 */
public interface ModuleNotificationService {

    /**
     * Send an in-app notification to a user
     */
    void sendInAppNotification(UUID userId, String title, String message, NotificationPriority priority);

    /**
     * Send an email notification
     */
    void sendEmailNotification(String email, String subject, String htmlBody);

    /**
     * Send email using a template
     */
    void sendEmailNotification(String email, String templateId, Map<String, Object> templateData);

    /**
     * Send notification to multiple users
     */
    void sendBulkNotification(List<UUID> userIds, String title, String message, NotificationPriority priority);

    /**
     * Send push notification
     */
    void sendPushNotification(UUID userId, String title, String body, Map<String, String> data);

    /**
     * Schedule a notification for later
     */
    String scheduleNotification(UUID userId, String title, String message,
                                NotificationPriority priority, java.time.Instant sendAt);

    /**
     * Cancel a scheduled notification
     */
    void cancelScheduledNotification(String notificationId);

    enum NotificationPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
