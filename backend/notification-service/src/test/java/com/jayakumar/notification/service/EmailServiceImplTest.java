package com.jayakumar.notification.service;

import com.jayakumar.notification.entity.DeliveryStatus;
import com.jayakumar.notification.entity.Notification;
import com.jayakumar.notification.entity.NotificationType;
import com.jayakumar.notification.exception.EmailDeliveryException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, "orders@example.com");
    }

    @Test
    void sendOrderNotificationBuildsAndSendsOrderConfirmedEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendOrderNotification(notification(
                NotificationType.ORDER_CONFIRMED,
                "customer@example.com"));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();
        assertEquals("Your order is confirmed", sentMessage.getSubject());
        assertEquals("orders@example.com", sentMessage.getFrom()[0].toString());
        assertEquals("customer@example.com", sentMessage.getAllRecipients()[0].toString());
        assertTrue(sentMessage.getContent().toString().contains("Order ID:</strong> 42"));
        assertTrue(sentMessage.getContent().toString().contains("Status:</strong> CONFIRMED"));
    }

    @Test
    void sendOrderNotificationWrapsMailFailureWithoutExposingSensitiveDetails() {
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        org.mockito.Mockito.doThrow(new MailSendException("SMTP authentication failed: secret"))
                .when(mailSender).send(any(MimeMessage.class));

        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> emailService.sendOrderNotification(notification(
                        NotificationType.ORDER_CANCELLED,
                        "customer@example.com")));

        assertEquals("Email delivery failed (MailSendException)", exception.getMessage());
        assertTrue(!exception.getMessage().contains("secret"));
    }

    @Test
    void sendOrderNotificationRejectsInvalidRecipientWithoutSending() {
        EmailDeliveryException exception = assertThrows(
                EmailDeliveryException.class,
                () -> emailService.sendOrderNotification(notification(
                        NotificationType.ORDER_CONFIRMED,
                        "invalid-email")));

        assertEquals("Recipient email address is missing or invalid", exception.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(mailSender, never()).createMimeMessage();
    }

    private Notification notification(NotificationType type, String recipientEmail) {
        LocalDateTime now = LocalDateTime.now();
        return new Notification(
                "notification-1",
                "event-1",
                42L,
                7L,
                type,
                DeliveryStatus.PENDING,
                recipientEmail,
                now,
                now,
                null);
    }
}
