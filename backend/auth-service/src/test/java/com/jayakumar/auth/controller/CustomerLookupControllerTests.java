package com.jayakumar.auth.controller;

import com.jayakumar.auth.entity.CustomerProfile;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import com.jayakumar.auth.service.EmailVerificationNotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "internal.api.token=test-internal-secret")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerLookupControllerTests {

    private static final String INTERNAL_TOKEN = "test-internal-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EmailVerificationNotificationClient notificationClient;

    @BeforeEach
    void clearProfiles() {
        customerProfileRepository.deleteAll();
    }

    @Test
    void returnsVerifiedCustomerFieldsForExistingCustomer() throws Exception {
        CustomerProfile profile = createVerifiedProfile();

        mockMvc.perform(get("/internal/customers/{customerId}", profile.getCustomerId())
                        .header("Authorization", "Bearer " + INTERNAL_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(profile.getCustomerId()))
                .andExpect(jsonPath("$.username").value("lookup-customer"))
                .andExpect(jsonPath("$.email").value("lookup@example.test"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.*", containsInAnyOrder(
                        profile.getCustomerId().intValue(), "lookup-customer",
                        "lookup@example.test", true)));
    }

    @Test
    void rejectsUnverifiedCustomerWithoutReturningEmail() throws Exception {
        CustomerProfile profile = createProfile();

        mockMvc.perform(get("/internal/customers/{customerId}", profile.getCustomerId())
                        .header("Authorization", "Bearer " + INTERNAL_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void returnsNotFoundForMissingCustomer() throws Exception {
        mockMvc.perform(get("/internal/customers/999999")
                        .header("Authorization", "Bearer " + INTERNAL_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsMissingOrInvalidServiceCredential() throws Exception {
        mockMvc.perform(get("/internal/customers/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/internal/customers/1")
                        .header("Authorization", "Bearer wrong-secret"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthorizationHeaderInCorsPreflight() throws Exception {
        mockMvc.perform(options("/internal/customers/1")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsStringIgnoringCase("authorization")))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void neverExposesPasswordHashOrVerificationTokenData() throws Exception {
        CustomerProfile profile = createVerifiedProfile();
        String response = mockMvc.perform(get(
                                "/internal/customers/{customerId}", profile.getCustomerId())
                        .header("Authorization", "Bearer " + INTERNAL_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.emailVerificationToken").doesNotExist())
                .andExpect(jsonPath("$.emailVerificationTokenHash").doesNotExist())
                .andExpect(jsonPath("$.emailVerificationTokenExpiresAt").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(response.contains(profile.getPasswordHash()));
        org.junit.jupiter.api.Assertions.assertFalse(response.contains("stored-verification-token-hash"));
    }

    @Test
    void rejectsNonPositiveCustomerId() throws Exception {
        mockMvc.perform(get("/internal/customers/0")
                        .header("Authorization", "Bearer " + INTERNAL_TOKEN))
                .andExpect(status().isBadRequest());
    }

    private CustomerProfile createProfile() {
        CustomerProfile profile = new CustomerProfile(
                "lookup-customer",
                "lookup@example.test",
                "$2a$10$stored-password-hash");
        profile.setEmailVerificationToken(
                "stored-verification-token-hash",
                LocalDateTime.now().plusHours(1));
        return customerProfileRepository.saveAndFlush(profile);
    }

    private CustomerProfile createVerifiedProfile() {
        CustomerProfile profile = createProfile();
        jdbcTemplate.update(
                "UPDATE customer_profiles SET email_verified = TRUE WHERE customer_id = ?",
                profile.getCustomerId());
        return profile;
    }
}
