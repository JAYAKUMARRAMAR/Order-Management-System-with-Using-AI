package com.jayakumar.notification.service;

import com.jayakumar.notification.entity.Notification;
import com.jayakumar.notification.entity.NotificationType;
import com.jayakumar.notification.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String senderAddress;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${notification.email.from}") String senderAddress) {
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
    }

    @Override
    public void sendOrderNotification(Notification notification) {
        validateRecipient(notification.getRecipientEmail());
        EmailTemplate template = templateFor(notification.getNotificationType());

        try {
            String html = loadTemplate(template.resourcePath())
                    .replace("{{orderId}}", notification.getOrderId().toString())
                    .replace("{{status}}", template.status());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(senderAddress);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(template.subject());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MailException exception) {
            throw new EmailDeliveryException(
                    "Email delivery failed (" + exception.getClass().getSimpleName() + ")");
        } catch (MessagingException | IOException exception) {
            throw new EmailDeliveryException(
                    "Email preparation failed (" + exception.getClass().getSimpleName() + ")");
        }
    }

    @Override
    public void sendEmailVerification(String recipientEmail, String verificationUrl) {
        validateRecipient(recipientEmail);
        String html = """
                <html><body>
                <p>Please verify your email address by clicking the link below.</p>
                <p><a href="%s">Verify email address</a></p>
                </body></html>
                """.formatted(HtmlUtils.htmlEscape(verificationUrl));
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(senderAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Verify your email address");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MailException exception) {
            throw new EmailDeliveryException(
                    "Email delivery failed (" + exception.getClass().getSimpleName() + ")");
        } catch (MessagingException exception) {
            throw new EmailDeliveryException(
                    "Email preparation failed (" + exception.getClass().getSimpleName() + ")");
        }
    }

    private void validateRecipient(String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new EmailDeliveryException("Recipient email address is missing or invalid");
        }

        try {
            InternetAddress[] addresses = InternetAddress.parse(recipientEmail, true);
            if (addresses.length != 1) {
                throw new EmailDeliveryException("Recipient email address is missing or invalid");
            }
            addresses[0].validate();
        } catch (AddressException exception) {
            throw new EmailDeliveryException("Recipient email address is missing or invalid");
        }
    }

    private EmailTemplate templateFor(NotificationType notificationType) {
        return switch (notificationType) {
            case ORDER_CONFIRMED -> new EmailTemplate(
                    "Your order is confirmed",
                    "CONFIRMED",
                    "templates/order-confirmed.html");
            case ORDER_CANCELLED -> new EmailTemplate(
                    "Your order has been cancelled",
                    "CANCELLED",
                    "templates/order-cancelled.html");
        };
    }

    private String loadTemplate(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private record EmailTemplate(String subject, String status, String resourcePath) {
    }
}
