package com.jayakumar.auth.service;

import com.jayakumar.auth.entity.CustomerProfile;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 32;

    private final CustomerProfileRepository customerProfileRepository;
    private final EmailVerificationNotificationClient notificationClient;
    private final long tokenExpirationMillis;
    private final String verificationEndpoint;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            CustomerProfileRepository customerProfileRepository,
            EmailVerificationNotificationClient notificationClient,
            @Value("${auth.email-verification.expiration:86400000}") long tokenExpirationMillis,
            @Value("${auth.email-verification.url:http://localhost:8090/auth/verify-email}")
            String verificationEndpoint) {
        this.customerProfileRepository = customerProfileRepository;
        this.notificationClient = notificationClient;
        this.tokenExpirationMillis = tokenExpirationMillis;
        this.verificationEndpoint = verificationEndpoint;
    }

    public String issueToken(CustomerProfile profile) {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        profile.setEmailVerificationToken(
                hashToken(token),
                LocalDateTime.now().plusNanos(tokenExpirationMillis * 1_000_000));
        return token;
    }

    public void sendVerificationEmail(String recipientEmail, String token) {
        String verificationUrl = UriComponentsBuilder.fromUriString(verificationEndpoint)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
        notificationClient.sendVerificationEmail(recipientEmail, verificationUrl);
    }

    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return customerProfileRepository.verifyEmailWithToken(
                hashToken(token),
                LocalDateTime.now()) == 1;
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
