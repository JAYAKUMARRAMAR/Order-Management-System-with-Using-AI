package com.jayakumar.notification.controller;

import com.jayakumar.notification.dto.CreateNotificationRequest;
import com.jayakumar.notification.dto.NotificationResponse;
import com.jayakumar.notification.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public NotificationResponse createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.createNotification(request);
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse getNotificationById(
            @PathVariable @NotBlank @Size(max = 128) String notificationId) {
        return notificationService.getNotificationById(notificationId);
    }

    @GetMapping("/order/{orderId}")
    public List<NotificationResponse> getNotificationsByOrderId(
            @PathVariable @Positive Long orderId) {
        return notificationService.getNotificationsByOrderId(orderId);
    }
}
