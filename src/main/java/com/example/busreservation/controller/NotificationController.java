package com.example.busreservation.controller;

import com.example.busreservation.model.Notification;
import com.example.busreservation.service.NotificationService;
import com.example.busreservation.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NotificationController - REST API for notification management
 * Endpoints for retrieving and managing passenger notifications
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Register NotificationService as observer for payment events
     * This is called after the bean is constructed
     */
    @PostConstruct
    public void init() {
        paymentService.registerObserver(notificationService);
        System.out.println("NotificationService registered as PaymentObserver");
    }

    /**
     * Get all notifications for a specific user
     * GET /api/notifications/user/{userNic}
     */
    @GetMapping("/user/{userNic}")
    public ResponseEntity<List<Map<String, Object>>> getNotificationsForUser(@PathVariable String userNic) {
        try {
            List<Notification> notifications = notificationService.getNotificationsForUser(userNic);
            List<Map<String, Object>> formattedNotifications = notifications.stream()
                .map(notificationService::formatNotification)
                .collect(Collectors.toList());
            return ResponseEntity.ok(formattedNotifications);
        } catch (Exception e) {
            System.err.println("Error retrieving notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread notifications for a specific user
     * GET /api/notifications/user/{userNic}/unread
     */
    @GetMapping("/user/{userNic}/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnreadNotifications(@PathVariable String userNic) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userNic);
            List<Map<String, Object>> formattedNotifications = notifications.stream()
                .map(notificationService::formatNotification)
                .collect(Collectors.toList());
            return ResponseEntity.ok(formattedNotifications);
        } catch (Exception e) {
            System.err.println("Error retrieving unread notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread notification count for a user
     * GET /api/notifications/user/{userNic}/count
     */
    @GetMapping("/user/{userNic}/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String userNic) {
        try {
            long count = notificationService.getUnreadCount(userNic);
            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error retrieving unread count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark a notification as read
     * PUT /api/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Integer notificationId) {
        try {
            boolean success = notificationService.markAsRead(notificationId);
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("message", "Notification marked as read");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Notification not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Mark all notifications as read for a user
     * PUT /api/notifications/user/{userNic}/read-all
     */
    @PutMapping("/user/{userNic}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable String userNic) {
        try {
            boolean success = notificationService.markAllAsRead(userNic);
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("message", "All notifications marked as read");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "No unread notifications found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

