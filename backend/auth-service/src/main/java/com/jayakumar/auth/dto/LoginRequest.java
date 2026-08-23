package com.jayakumar.auth.dto;

public record LoginRequest(
        String username,
        String password) {
}