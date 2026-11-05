package com.jayakumar.notification.dto;

import com.jayakumar.notification.entity.DeliveryStatus;
import com.jayakumar.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        String notificationId,
        String eventId,
        Long orderId,
        NotificationType notificationType,
        DeliveryStatus deliveryStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
