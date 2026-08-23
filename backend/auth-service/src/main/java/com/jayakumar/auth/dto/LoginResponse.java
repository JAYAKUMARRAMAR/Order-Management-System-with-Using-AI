package com.jayakumar.auth.dto;

public record LoginResponse(
        String token,
        String username) {
}