package com.jayakumar.auth.repository;

import com.jayakumar.auth.entity.CustomerProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class CustomerProfileRepositoryTests {

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Test
    void savesProfileWithGeneratedIdAndTimestamps() {
        CustomerProfile savedProfile = customerProfileRepository.saveAndFlush(
                new CustomerProfile("profile-user", "profile@example.test", "encoded-password-hash")
        );

        assertNotNull(savedProfile.getCustomerId());
        assertFalse(savedProfile.isEmailVerified());
        assertNotNull(savedProfile.getCreatedAt());
        assertNotNull(savedProfile.getUpdatedAt());
        assertEquals(savedProfile.getCreatedAt(), savedProfile.getUpdatedAt());
        assertEquals(savedProfile, customerProfileRepository.findById(savedProfile.getCustomerId()).orElseThrow());
    }

    @Test
    void rejectsDuplicateUsername() {
        customerProfileRepository.saveAndFlush(
                new CustomerProfile("duplicate-user", "first@example.test", "encoded-password-hash")
        );

        assertThrows(DataIntegrityViolationException.class, () ->
                customerProfileRepository.saveAndFlush(
                        new CustomerProfile("duplicate-user", "second@example.test", "encoded-password-hash")
                )
        );
    }

    @Test
    void rejectsDuplicateEmail() {
        customerProfileRepository.saveAndFlush(
                new CustomerProfile("first-user", "duplicate@example.test", "encoded-password-hash")
        );

        assertThrows(DataIntegrityViolationException.class, () ->
                customerProfileRepository.saveAndFlush(
                        new CustomerProfile("second-user", "duplicate@example.test", "encoded-password-hash")
                )
        );
    }
}
