package com.jayakumar.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(max = 100)
        String username,
        @NotBlank
        @Size(max = 72)
        String password) {

    public LoginRequest {
        username = AuthInputNormalizer.normalize(username);
    }

    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=[REDACTED]]";
    }
}