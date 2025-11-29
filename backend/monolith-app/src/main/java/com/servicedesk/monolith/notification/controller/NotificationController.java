package com.servicedesk.monolith.notification.controller;

import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.dto.PageResponse;
import com.servicedesk.common.security.SecurityUtils;
import com.servicedesk.monolith.notification.dto.NotificationDto;
import com.servicedesk.monolith.notification.dto.SendNotificationRequest;
import com.servicedesk.monolith.notification.entity.NotificationPreference;
import com.servicedesk.monolith.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userId = SecurityUtils.getCurrentUserId();
        Page<NotificationDto> notifications = notificationService.getUserNotifications(
                userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        PageResponse<NotificationDto> response = new PageResponse<>(
                notifications.getContent(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadNotifications(userId)));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(userId)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markAsRead(id)));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/read")
    public ResponseEntity<ApiResponse<Void>> deleteAllRead() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreference>> getPreferences() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getPreferences(userId)));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreference>> updatePreferences(
            @RequestBody NotificationPreference preferences) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.updatePreferences(userId, preferences)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationDto>> sendNotification(
            @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.createNotification(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> sendBulkNotifications(
            @RequestBody List<SendNotificationRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.createBulkNotifications(requests)));
    }
}
