package com.jayakumar.notification.service;

import com.jayakumar.notification.dto.CreateNotificationRequest;
import com.jayakumar.notification.dto.NotificationResponse;
import com.jayakumar.notification.entity.DeliveryStatus;
import com.jayakumar.notification.entity.Notification;
import com.jayakumar.notification.entity.NotificationType;
import com.jayakumar.notification.exception.EmailDeliveryException;
import com.jayakumar.notification.exception.NotificationNotFoundException;
import com.jayakumar.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotificationSendsEmailAndPersistsSentStatus() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "event-1",
                42L,
                7L,
                NotificationType.ORDER_CONFIRMED,
                "customer@example.com");
        when(notificationRepository.findByEventId("event-1")).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.createNotification(request);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getAllValues().get(0);
        assertEquals(DeliveryStatus.SENT, savedNotification.getDeliveryStatus());
        assertEquals("customer@example.com", savedNotification.getRecipientEmail());
        assertNotNull(savedNotification.getCreatedAt());
        assertNotNull(savedNotification.getUpdatedAt());
        assertFalse(savedNotification.getUpdatedAt().isBefore(savedNotification.getCreatedAt()));
        assertEquals(DeliveryStatus.SENT, response.deliveryStatus());
        assertEquals("event-1", response.eventId());
        verify(emailService).sendOrderNotification(savedNotification);
    }

    @Test
    void createNotificationMarksDeliveryFailedWhenEmailSendingFails() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "event-2",
                43L,
                7L,
                NotificationType.ORDER_CANCELLED,
                "customer@example.com");
        when(notificationRepository.findByEventId("event-2")).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new EmailDeliveryException("Email delivery failed (MailSendException)"))
                .when(emailService).sendOrderNotification(any(Notification.class));

        NotificationResponse response = notificationService.createNotification(request);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(notificationCaptor.capture());
        Notification persistedNotification = notificationCaptor.getAllValues().get(1);
        assertEquals(DeliveryStatus.FAILED, persistedNotification.getDeliveryStatus());
        assertEquals("Email delivery failed (MailSendException)", persistedNotification.getFailureReason());
        assertEquals(DeliveryStatus.FAILED, response.deliveryStatus());
    }

    @Test
    void createNotificationReturnsExistingRecordForDuplicateEvent() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "event-1",
                42L,
                7L,
                NotificationType.ORDER_CONFIRMED,
                null);
        Notification existingNotification = notification(
                "notification-1", "event-1", 42L, DeliveryStatus.PENDING);
        when(notificationRepository.findByEventId("event-1"))
                .thenReturn(Optional.of(existingNotification));

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals("notification-1", response.notificationId());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(emailService, never()).sendOrderNotification(any(Notification.class));
    }

    @Test
    void createNotificationReturnsConcurrentDuplicateRecord() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "event-1",
                42L,
                7L,
                NotificationType.ORDER_CONFIRMED,
                null);
        Notification existingNotification = notification(
                "notification-1", "event-1", 42L, DeliveryStatus.PENDING);
        when(notificationRepository.findByEventId("event-1"))
                .thenReturn(Optional.empty(), Optional.of(existingNotification));
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new DuplicateKeyException("duplicate eventId"));

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals("notification-1", response.notificationId());
        verify(notificationRepository).save(any(Notification.class));
        verify(emailService, never()).sendOrderNotification(any(Notification.class));
    }

    @Test
    void getNotificationByIdThrowsWhenNotFound() {
        when(notificationRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.getNotificationById("missing"));
    }

    @Test
    void getNotificationsByOrderIdReturnsMappedNotifications() {
        Notification notification = notification(
                "notification-1", "event-1", 42L, DeliveryStatus.PENDING);
        when(notificationRepository.findAllByOrderIdOrderByCreatedAtDesc(42L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotificationsByOrderId(42L);

        assertEquals(1, responses.size());
        assertEquals("notification-1", responses.get(0).notificationId());
        verify(notificationRepository).findAllByOrderIdOrderByCreatedAtDesc(42L);
    }

    private Notification notification(
            String notificationId,
            String eventId,
            Long orderId,
            DeliveryStatus deliveryStatus) {
        LocalDateTime now = LocalDateTime.now();
        return new Notification(
                notificationId,
                eventId,
                orderId,
                7L,
                NotificationType.ORDER_CONFIRMED,
                deliveryStatus,
                "customer@example.com",
                now,
                now,
                null);
    }
}
