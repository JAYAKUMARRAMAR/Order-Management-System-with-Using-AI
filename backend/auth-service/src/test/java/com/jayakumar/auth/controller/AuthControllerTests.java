package com.jayakumar.auth.controller;

import com.jayakumar.auth.entity.CustomerProfile;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void logsInEligibleCustomerWithExistingJwtResponseShape() throws Exception {
        register("login-user", "login@example.test");
        jdbcTemplate.update(
                "UPDATE customer_profiles SET email_verified = TRUE WHERE username = ?",
                "login-user");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(" LOGIN-USER ", "correct-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.username").value("login-user"))
                .andExpect(jsonPath("$.customerId").doesNotExist());
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
}
