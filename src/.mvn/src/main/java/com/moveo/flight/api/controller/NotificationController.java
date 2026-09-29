package com.moveo.flight.api.controller;

import com.moveo.flight.api.dto.NotificationDTO;
import com.moveo.flight.api.model.Notification;
import com.moveo.flight.api.model.NotificationType;
import com.moveo.flight.api.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to manage notifications
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
    *  Create a notification for a user.
    */
    @PostMapping("/{userId}")
    public ResponseEntity<NotificationDTO> createNotification(
            @PathVariable Long userId,
            @RequestParam NotificationType type,
            @RequestParam String message) {

        Notification notification = notificationService.createNotification(userId, type, message);
        return ResponseEntity.ok(notificationService.toDTO(notification));
    }

    /** ✅ Get all notifications for a user */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(notificationService::toDTO)
                .toList();
        return ResponseEntity.ok(notificationDTOs);
    }
}
