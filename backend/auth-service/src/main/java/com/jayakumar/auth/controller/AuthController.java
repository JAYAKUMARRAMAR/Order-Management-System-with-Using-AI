package com.jayakumar.auth.controller;

import com.jayakumar.auth.dto.AuthErrorResponse;
import com.jayakumar.auth.dto.EmailVerificationResponse;
import com.jayakumar.auth.dto.LoginRequest;
import com.jayakumar.auth.dto.LoginResponse;
import com.jayakumar.auth.dto.RegistrationRequest;
import com.jayakumar.auth.dto.RegistrationResponse;
import com.jayakumar.auth.entity.CustomerProfile;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import com.jayakumar.auth.service.JwtService;
import com.jayakumar.auth.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final CustomerProfileRepository customerProfileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            JwtService jwtService,
            CustomerProfileRepository customerProfileRepository,
            BCryptPasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.jwtService = jwtService;
        this.customerProfileRepository = customerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest()
                    .body(new AuthErrorResponse("Invalid request"));
        }

        if (customerProfileRepository.existsByUsername(request.username())
                || customerProfileRepository.existsByEmail(request.email())) {
            return registrationConflict();
        }

        CustomerProfile profile = new CustomerProfile(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        String verificationToken = emailVerificationService.issueToken(profile);

        try {
            CustomerProfile savedProfile = customerProfileRepository.saveAndFlush(profile);
            emailVerificationService.sendVerificationEmail(
                    savedProfile.getEmail(),
                    verificationToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new RegistrationResponse(
                            savedProfile.getCustomerId(),
                            savedProfile.getUsername(),
                            savedProfile.getEmail()
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            return registrationConflict();
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        if (!emailVerificationService.verifyToken(token)) {
            return ResponseEntity.badRequest()
                    .body(new AuthErrorResponse("Invalid or expired email verification token"));
        }
        return ResponseEntity.ok(new EmailVerificationResponse("Email verified successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return loginFailure();
        }

        CustomerProfile profile = customerProfileRepository.findByUsername(request.username())
                .orElse(null);
        if (profile == null
                || !passwordEncoder.matches(request.password(), profile.getPasswordHash())
                || !profile.isEmailVerified()) {
            return loginFailure();
        }

        String token = jwtService.generateToken(profile.getUsername(), profile.getCustomerId());
        return ResponseEntity.ok(new LoginResponse(token, profile.getUsername()));
    }

    private static ResponseEntity<AuthErrorResponse> registrationConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthErrorResponse("Username or email is already registered"));
    }

    private static ResponseEntity<AuthErrorResponse> loginFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorResponse("Invalid username or password"));
    }
}