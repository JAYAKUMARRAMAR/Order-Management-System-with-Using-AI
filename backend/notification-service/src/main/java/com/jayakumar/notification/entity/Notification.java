package com.jayakumar.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String notificationId;

    @Indexed(unique = true)
    private String eventId;

    private Long orderId;

    private Long customerId;

    private NotificationType notificationType;

    private DeliveryStatus deliveryStatus;

    private String recipientEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String failureReason;
}
