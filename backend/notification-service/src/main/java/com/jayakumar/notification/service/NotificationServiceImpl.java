package com.jayakumar.notification.service;

import com.jayakumar.notification.dto.CreateNotificationRequest;
import com.jayakumar.notification.dto.NotificationResponse;
import com.jayakumar.notification.entity.DeliveryStatus;
import com.jayakumar.notification.entity.Notification;
import com.jayakumar.notification.exception.EmailDeliveryException;
import com.jayakumar.notification.exception.NotificationNotFoundException;
import com.jayakumar.notification.repository.NotificationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        String eventId = request.eventId().trim();
        Notification existingNotification = notificationRepository.findByEventId(eventId)
                .orElse(null);
        if (existingNotification != null) {
            return toResponse(existingNotification);
        }

        LocalDateTime now = LocalDateTime.now();
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                eventId,
                request.orderId(),
                request.customerId(),
                request.notificationType(),
                DeliveryStatus.PENDING,
                request.recipientEmail(),
                now,
                now,
                null);

        try {
            notification = notificationRepository.save(notification);
        } catch (DuplicateKeyException exception) {
            return notificationRepository.findByEventId(eventId)
                    .map(this::toResponse)
                    .orElseThrow(() -> exception);
        }

        try {
            emailService.sendOrderNotification(notification);
            notification.setDeliveryStatus(DeliveryStatus.SENT);
            notification.setFailureReason(null);
        } catch (EmailDeliveryException exception) {
            notification.setDeliveryStatus(DeliveryStatus.FAILED);
            notification.setFailureReason(exception.getMessage());
        }
        notification.setUpdatedAt(LocalDateTime.now());
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    @Override
    public List<NotificationResponse> getNotificationsByOrderId(Long orderId) {
        return notificationRepository.findAllByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getEventId(),
                notification.getOrderId(),
                notification.getNotificationType(),
                notification.getDeliveryStatus(),
                notification.getCreatedAt(),
                notification.getUpdatedAt());
    }
}
