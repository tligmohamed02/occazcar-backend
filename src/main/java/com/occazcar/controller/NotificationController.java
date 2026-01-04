package com.occazcar.controller;

import com.occazcar.dto.NotificationResponse;
import com.occazcar.model.Notification;
import com.occazcar.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            logger.info("Récupération des notifications pour l'utilisateur: {}", userId);

            List<Notification> notifications = notificationService.getUserNotifications(userId);
            List<NotificationResponse> response = notifications.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Erreur lors du chargement des notifications: " + e.getMessage());
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            List<NotificationResponse> response = notifications.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications non lues: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des notifications: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            notificationService.markAsRead(id, userId);
            return ResponseEntity.ok("Notification marquée comme lue");
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de la notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok("Toutes les notifications ont été marquées comme lues");
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de toutes les notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setRelatedEntityId(notification.getRelatedEntityId());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt().toString());
        return response;
    }
}