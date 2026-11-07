package com.jayakumar.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerificationRequest(
        @NotBlank @Email @Size(max = 254) String recipientEmail,
        @NotBlank @Size(max = 2048) String verificationUrl) {
}
