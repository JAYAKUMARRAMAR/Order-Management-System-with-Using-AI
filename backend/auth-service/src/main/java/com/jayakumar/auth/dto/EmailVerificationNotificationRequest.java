package com.jayakumar.auth.dto;

public record EmailVerificationNotificationRequest(
        String recipientEmail,
        String verificationUrl) {
}
