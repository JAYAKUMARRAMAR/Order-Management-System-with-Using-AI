package com.jayakumar.auth.dto;

import com.jayakumar.auth.entity.CustomerProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerLookupResponse(
        @NotNull Long customerId,
        @NotBlank String username,
        @NotBlank @Email String email,
        boolean emailVerified) {

    public static CustomerLookupResponse from(CustomerProfile profile) {
        return new CustomerLookupResponse(
                profile.getCustomerId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.isEmailVerified());
    }
}
