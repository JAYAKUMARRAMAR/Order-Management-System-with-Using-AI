package com.jayakumar.auth.controller;

import com.jayakumar.auth.dto.LoginRequest;
import com.jayakumar.auth.dto.LoginResponse;

import com.jayakumar.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {

        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        /*
         * Demo credentials.
         *
         * Later we will replace this with
         * MySQL user authentication.
         */

        if ("admin".equals(request.username())
                && "admin123".equals(request.password())) {

            String token =
                    jwtService.generateToken(
                            request.username()
                    );

            return ResponseEntity.ok(
                    new LoginResponse(
                            token,
                            request.username()
                    )
            );
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
    }
}