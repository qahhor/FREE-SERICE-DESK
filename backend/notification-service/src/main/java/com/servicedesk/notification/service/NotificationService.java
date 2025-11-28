package com.servicedesk.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.notification.dto.NotificationDto;
import com.servicedesk.notification.dto.SendNotificationRequest;
import com.servicedesk.notification.entity.Notification;
import com.servicedesk.notification.entity.NotificationPreference;
import com.servicedesk.notification.repository.NotificationPreferenceRepository;
import com.servicedesk.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailNotificationService emailService;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationDto createNotification(SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .actionUrl(request.getActionUrl())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .priority(request.getPriority() != null ? request.getPriority() : Notification.Priority.NORMAL)
                .isRead(false)
                .build();

        if (request.getMetadata() != null) {
            try {
                notification.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            } catch (Exception e) {
                log.error("Failed to serialize metadata", e);
            }
        }

        notification = notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);

        // Send real-time notification via WebSocket
        sendWebSocketNotification(request.getUserId(), dto);

        // Send email notification if enabled
        sendEmailNotificationAsync(notification);

        return dto;
    }

    @Transactional
    public List<NotificationDto> createBulkNotifications(List<SendNotificationRequest> requests) {
        return requests.stream()
                .map(this::createNotification)
                .collect(Collectors.toList());
    }

    public Page<NotificationDto> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    public List<NotificationDto> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDto markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        return toDto(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllRead(String userId) {
        notificationRepository.deleteByUserIdAndIsReadTrue(userId);
    }

    public NotificationPreference getPreferences(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    @Transactional
    public NotificationPreference updatePreferences(String userId, NotificationPreference preferences) {
        NotificationPreference existing = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference newPref = new NotificationPreference();
                    newPref.setUserId(userId);
                    return newPref;
                });

        // Copy non-null values
        if (preferences.getEmailEnabled() != null) existing.setEmailEnabled(preferences.getEmailEnabled());
        if (preferences.getEmailTicketAssigned() != null) existing.setEmailTicketAssigned(preferences.getEmailTicketAssigned());
        if (preferences.getEmailTicketUpdated() != null) existing.setEmailTicketUpdated(preferences.getEmailTicketUpdated());
        if (preferences.getEmailTicketCommented() != null) existing.setEmailTicketCommented(preferences.getEmailTicketCommented());
        if (preferences.getEmailMentions() != null) existing.setEmailMentions(preferences.getEmailMentions());
        if (preferences.getEmailSlaAlerts() != null) existing.setEmailSlaAlerts(preferences.getEmailSlaAlerts());
        if (preferences.getInappEnabled() != null) existing.setInappEnabled(preferences.getInappEnabled());
        if (preferences.getInappSound() != null) existing.setInappSound(preferences.getInappSound());
        if (preferences.getPushEnabled() != null) existing.setPushEnabled(preferences.getPushEnabled());
        if (preferences.getPushToken() != null) existing.setPushToken(preferences.getPushToken());
        if (preferences.getSmsEnabled() != null) existing.setSmsEnabled(preferences.getSmsEnabled());
        if (preferences.getSmsPhone() != null) existing.setSmsPhone(preferences.getSmsPhone());
        if (preferences.getQuietHoursEnabled() != null) existing.setQuietHoursEnabled(preferences.getQuietHoursEnabled());
        if (preferences.getQuietHoursStart() != null) existing.setQuietHoursStart(preferences.getQuietHoursStart());
        if (preferences.getQuietHoursEnd() != null) existing.setQuietHoursEnd(preferences.getQuietHoursEnd());

        return preferenceRepository.save(existing);
    }

    private NotificationPreference createDefaultPreferences(String userId) {
        NotificationPreference preferences = NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .emailTicketAssigned(true)
                .emailTicketUpdated(true)
                .emailTicketCommented(true)
                .emailMentions(true)
                .emailSlaAlerts(true)
                .inappEnabled(true)
                .inappSound(true)
                .pushEnabled(false)
                .smsEnabled(false)
                .quietHoursEnabled(false)
                .build();

        return preferenceRepository.save(preferences);
    }

    private void sendWebSocketNotification(String userId, NotificationDto notification) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
            log.debug("WebSocket notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Async
    public void sendEmailNotificationAsync(Notification notification) {
        try {
            NotificationPreference preferences = getPreferences(notification.getUserId());

            if (!Boolean.TRUE.equals(preferences.getEmailEnabled())) {
                return;
            }

            boolean shouldSend = switch (notification.getType()) {
                case TICKET_ASSIGNED -> Boolean.TRUE.equals(preferences.getEmailTicketAssigned());
                case TICKET_UPDATED -> Boolean.TRUE.equals(preferences.getEmailTicketUpdated());
                case TICKET_COMMENTED -> Boolean.TRUE.equals(preferences.getEmailTicketCommented());
                case TICKET_MENTIONED, TEAM_MENTION -> Boolean.TRUE.equals(preferences.getEmailMentions());
                case SLA_WARNING, SLA_BREACHED -> Boolean.TRUE.equals(preferences.getEmailSlaAlerts());
                default -> true;
            };

            if (shouldSend) {
                emailService.sendNotificationEmail(notification);
            }
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "notification.queue")
    public void handleNotificationEvent(Map<String, Object> event) {
        try {
            SendNotificationRequest request = objectMapper.convertValue(event, SendNotificationRequest.class);
            createNotification(request);
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
        }
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionUrl(notification.getActionUrl())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .priority(notification.getPriority())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
