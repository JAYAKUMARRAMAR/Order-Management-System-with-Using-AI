package com.jayakumar.auth.service;

import com.jayakumar.auth.entity.CustomerProfile;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTests {

    private CustomerProfileRepository customerProfileRepository;
    private EmailVerificationNotificationClient notificationClient;
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        customerProfileRepository = mock(CustomerProfileRepository.class);
        notificationClient = mock(EmailVerificationNotificationClient.class);
        emailVerificationService = new EmailVerificationService(
                customerProfileRepository,
                notificationClient,
                86_400_000L,
                "https://example.test/auth/verify-email");
    }

    @Test
    void issuesRandomUrlSafeTokenAndStoresOnlyItsSha256HashFor24Hours() throws Exception {
        CustomerProfile firstProfile = new CustomerProfile(
                "first-user", "first@example.test", "password-hash");
        CustomerProfile secondProfile = new CustomerProfile(
                "second-user", "second@example.test", "password-hash");
        LocalDateTime beforeIssue = LocalDateTime.now();

        String firstToken = emailVerificationService.issueToken(firstProfile);
        String secondToken = emailVerificationService.issueToken(secondProfile);

        LocalDateTime afterIssue = LocalDateTime.now();
        assertTrue(firstToken.matches("[A-Za-z0-9_-]{43}"));
        assertNotEquals(firstToken, secondToken);
        assertEquals(sha256(firstToken), firstProfile.getEmailVerificationTokenHash());
        assertNotEquals(firstToken, firstProfile.getEmailVerificationTokenHash());
        assertTrue(firstProfile.getEmailVerificationTokenExpiresAt()
                .isAfter(beforeIssue.plusHours(24).minusNanos(1)));
        assertTrue(firstProfile.getEmailVerificationTokenExpiresAt()
                .isBefore(afterIssue.plusHours(24).plusNanos(1)));
    }

    @Test
    void sendsVerificationUrlThroughNotificationClient() {
        CustomerProfile profile = new CustomerProfile(
                "mail-user", "mail@example.test", "password-hash");
        String token = emailVerificationService.issueToken(profile);

        emailVerificationService.sendVerificationEmail(profile.getEmail(), token);

        verify(notificationClient).sendVerificationEmail(
                eq("mail@example.test"),
                eq("https://example.test/auth/verify-email?token=" + token));
    }

    @Test
    void verifiesByHashAndRejectsUnknownTokens() throws Exception {
        when(customerProfileRepository.verifyEmailWithToken(
                any(String.class), any(LocalDateTime.class))).thenReturn(1, 0);

        assertTrue(emailVerificationService.verifyToken("valid-token"));
        assertFalse(emailVerificationService.verifyToken("unknown-token"));

        verify(customerProfileRepository).verifyEmailWithToken(
                eq(sha256("valid-token")), any(LocalDateTime.class));
        verify(customerProfileRepository).verifyEmailWithToken(
                eq(sha256("unknown-token")), any(LocalDateTime.class));
    }

    @Test
    void rejectsBlankTokensWithoutQueryingRepository() {
        assertFalse(emailVerificationService.verifyToken(null));
        assertFalse(emailVerificationService.verifyToken(""));
        assertFalse(emailVerificationService.verifyToken("   "));

        verify(customerProfileRepository, never())
                .verifyEmailWithToken(any(String.class), any(LocalDateTime.class));
    }

    private static String sha256(String token) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
