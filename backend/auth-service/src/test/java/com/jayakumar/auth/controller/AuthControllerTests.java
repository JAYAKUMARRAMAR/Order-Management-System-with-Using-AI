package com.jayakumar.auth.controller;

import com.jayakumar.auth.entity.CustomerProfile;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import com.jayakumar.auth.service.EmailVerificationNotificationClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EmailVerificationNotificationClient notificationClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @BeforeEach
    void clearProfiles() {
        customerProfileRepository.deleteAll();
    }

    @Test
    void registersNormalizedCustomerWithoutExposingPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": " NewCustomer ",
                                  "email": " NewCustomer@Example.Test ",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").isNumber())
                .andExpect(jsonPath("$.username").value("newcustomer"))
                .andExpect(jsonPath("$.email").value("newcustomer@example.test"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsDuplicateNormalizedUsername() throws Exception {
        register("duplicate-user", "first@example.test");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(" DUPLICATE-USER ", "second@example.test")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsDuplicateNormalizedEmail() throws Exception {
        register("first-user", "duplicate@example.test");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson("second-user", " DUPLICATE@example.test ")))
                .andExpect(status().isConflict());
    }

    @Test
    void storesPasswordAsBcryptHash() throws Exception {
        register("hashed-user", "hashed@example.test");

        CustomerProfile profile = customerProfileRepository.findByUsername("hashed-user")
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals("correct-password", profile.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(profile.getPasswordHash().startsWith("$2"));
        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches("correct-password", profile.getPasswordHash()));
        org.junit.jupiter.api.Assertions.assertFalse(profile.isEmailVerified());
    }

    @Test
    void registersVerifiesEmailAndAllowsLoginWithoutChangingJwtShape() throws Exception {
        register("verify-user", "verify@example.test");

        ArgumentCaptor<String> verificationUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationClient).sendVerificationEmail(
                eq("verify@example.test"),
                verificationUrlCaptor.capture());
        String verificationToken = tokenFromUrl(verificationUrlCaptor.getValue());

        CustomerProfile pendingProfile = customerProfileRepository.findByUsername("verify-user")
                .orElseThrow();
        assertFalse(pendingProfile.isEmailVerified());
        assertNotEquals(verificationToken, pendingProfile.getEmailVerificationTokenHash());
        assertEquals(64, pendingProfile.getEmailVerificationTokenHash().length());
        assertTrue(pendingProfile.getEmailVerificationTokenExpiresAt().isAfter(
                java.time.LocalDateTime.now()));

        mockMvc.perform(get("/auth/verify-email").param("token", verificationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));

        CustomerProfile verifiedProfile = customerProfileRepository.findByUsername("verify-user")
                .orElseThrow();
        assertTrue(verifiedProfile.isEmailVerified());
        assertNull(verifiedProfile.getEmailVerificationTokenHash());
        assertNull(verifiedProfile.getEmailVerificationTokenExpiresAt());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("verify-user", "correct-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.username").value("verify-user"))
                .andExpect(jsonPath("$.customerId").doesNotExist());
    }

    @Test
    void rejectsExpiredEmailVerificationToken() throws Exception {
        register("expired-user", "expired@example.test");
        String token = verificationTokenSentTo("expired@example.test");
        jdbcTemplate.update(
                "UPDATE customer_profiles SET email_verification_token_expires_at = ? WHERE username = ?",
                java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().minusMinutes(1)),
                "expired-user");

        mockMvc.perform(get("/auth/verify-email").param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired email verification token"));
        assertFalse(customerProfileRepository.findByUsername("expired-user")
                .orElseThrow().isEmailVerified());
    }

    @Test
    void rejectsReusedEmailVerificationToken() throws Exception {
        register("reused-user", "reused@example.test");
        String token = verificationTokenSentTo("reused@example.test");

        mockMvc.perform(get("/auth/verify-email").param("token", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/auth/verify-email").param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired email verification token"));
    }

    @Test
    void rejectsUnknownEmailVerificationToken() throws Exception {
        mockMvc.perform(get("/auth/verify-email").param("token", "not-a-valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired email verification token"));
    }

    @Test
    void logsInEligibleCustomerWithExistingJwtResponseShape() throws Exception {
        register("login-user", "login@example.test");
        jdbcTemplate.update(
                "UPDATE customer_profiles SET email_verified = TRUE WHERE username = ?",
                "login-user");
        Long customerId = customerProfileRepository.findByUsername("login-user")
                .orElseThrow()
                .getCustomerId();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(" LOGIN-USER ", "correct-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.username").value("login-user"))
                .andExpect(jsonPath("$.customerId").doesNotExist())
                .andDo(result -> {
                    String token = objectMapper.readTree(result.getResponse().getContentAsString())
                            .get("token").asText();
                    Claims claims = parseClaims(token);
                    assertEquals("login-user", claims.getSubject());
                    assertEquals(customerId.longValue(), claims.get("customerId", Long.class));
                    assertEquals(jwtExpiration,
                            claims.getExpiration().getTime() - claims.getIssuedAt().getTime(),
                            1000L);
                });
    }

    @Test
    void ignoresClientSuppliedCustomerIdWhenCreatingJwt() throws Exception {
        register("spoof-user", "spoof@example.test");
        jdbcTemplate.update(
                "UPDATE customer_profiles SET email_verified = TRUE WHERE username = ?",
                "spoof-user");
        Long customerId = customerProfileRepository.findByUsername("spoof-user")
                .orElseThrow()
                .getCustomerId();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "spoof-user",
                                  "password": "correct-password",
                                  "customerId": 999999
                                }
                                """))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String token = objectMapper.readTree(result.getResponse().getContentAsString())
                            .get("token").asText();
                    assertEquals(customerId.longValue(),
                            parseClaims(token).get("customerId", Long.class));
                });
    }

    @Test
    void rejectsTokensWithInvalidSignaturesAndExpiredTokens() {
        String validToken = Jwts.builder()
                .subject("validation-user")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(jwtSigningKey())
                .compact();

        assertEquals("validation-user", parseClaims(validToken).getSubject());

        SecretKey otherKey = Keys.hmacShaKeyFor(
                "another-signing-key-that-is-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        assertThrows(SignatureException.class, () -> Jwts.parser()
                .verifyWith(otherKey)
                .build()
                .parseSignedClaims(validToken));

        String expiredToken = Jwts.builder()
                .subject("expired-user")
                .issuedAt(new Date(System.currentTimeMillis() - 20_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(jwtSigningKey())
                .compact();

        assertThrows(ExpiredJwtException.class, () -> parseClaims(expiredToken));
    }

    @Test
    void rejectsInvalidPassword() throws Exception {
        createVerifiedProfile("login-user", "login@example.test", "correct-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login-user", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void rejectsUnknownUsername() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("unknown-user", "correct-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void rejectsUnverifiedAccount() throws Exception {
        register("unverified-user", "unverified@example.test");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("unverified-user", "correct-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void validatesRegistrationFields() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": " ",
                                  "email": "not-an-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"));
    }

    private void register(String username, String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(username, email)))
                .andExpect(status().isCreated());
    }

    private String verificationTokenSentTo(String email) {
        ArgumentCaptor<String> verificationUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationClient).sendVerificationEmail(eq(email), verificationUrlCaptor.capture());
        return tokenFromUrl(verificationUrlCaptor.getValue());
    }

    private static String tokenFromUrl(String verificationUrl) {
        String query = URI.create(verificationUrl).getRawQuery();
        return java.util.Arrays.stream(query.split("&"))
                .filter(parameter -> parameter.startsWith("token="))
                .map(parameter -> URLDecoder.decode(
                        parameter.substring("token=".length()),
                        StandardCharsets.UTF_8))
                .findFirst()
                .orElseThrow();
    }

    private void createVerifiedProfile(String username, String email, String password) {
        customerProfileRepository.saveAndFlush(new CustomerProfile(
                username,
                email,
                passwordEncoder.encode(password)
        ));
        jdbcTemplate.update(
                "UPDATE customer_profiles SET email_verified = TRUE WHERE username = ?",
                username);
    }

    private static String registrationJson(String username, String email) {
        return """
                {"username":"%s","email":"%s","password":"correct-password"}
                """.formatted(username, email);
    }

    private static String loginJson(String username, String password) {
        return """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey jwtSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
