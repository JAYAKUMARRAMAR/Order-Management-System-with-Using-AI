package com.jayakumar.notification.dto;

import com.jayakumar.notification.entity.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotBlank @Size(max = 128) String eventId,
        @NotNull @Positive Long orderId,
        @NotNull @Positive Long customerId,
        @NotNull NotificationType notificationType,
        @Email @Size(max = 254) String recipientEmail) {
}
