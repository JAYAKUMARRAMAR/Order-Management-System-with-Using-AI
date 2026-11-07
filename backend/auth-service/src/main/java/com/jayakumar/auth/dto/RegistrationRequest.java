package com.jayakumar.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank
        @Size(max = 100)
        String username,
        @NotBlank
        @Email
        @Size(max = 254)
        String email,
        @NotBlank
        @Size(max = 72)
        String password) {

    public RegistrationRequest {
        username = AuthInputNormalizer.normalize(username);
        email = AuthInputNormalizer.normalize(email);
    }

    @Override
    public String toString() {
        return "RegistrationRequest[username=" + username
                + ", email=" + email + ", password=[REDACTED]]";
    }
}
