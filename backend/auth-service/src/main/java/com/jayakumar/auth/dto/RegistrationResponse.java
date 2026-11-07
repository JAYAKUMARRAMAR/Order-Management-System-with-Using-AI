package com.jayakumar.auth.dto;

public record RegistrationResponse(
        Long customerId,
        String username,
        String email) {
}
