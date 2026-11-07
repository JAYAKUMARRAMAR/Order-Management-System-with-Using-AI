package com.jayakumar.auth.service;

import com.jayakumar.auth.dto.EmailVerificationNotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmailVerificationNotificationClient {

    private final RestClient restClient;

    public EmailVerificationNotificationClient(
            @Value("${notification.service.url:http://localhost:8084}") String serviceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .build();
    }

    public void sendVerificationEmail(String recipientEmail, String verificationUrl) {
        restClient.post()
                .uri("/api/notifications/email-verification")
                .body(new EmailVerificationNotificationRequest(recipientEmail, verificationUrl))
                .retrieve()
                .toBodilessEntity();
    }
}
