package com.jayakumar.auth.repository;

import com.jayakumar.auth.entity.CustomerProfile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<CustomerProfile> findByUsername(String username);

    Optional<CustomerProfile> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            UPDATE CustomerProfile profile
            SET profile.emailVerified = true,
                profile.emailVerificationTokenHash = null,
                profile.emailVerificationTokenExpiresAt = null
            WHERE profile.emailVerificationTokenHash = :tokenHash
              AND profile.emailVerificationTokenExpiresAt > :now
              AND profile.emailVerified = false
            """)
    int verifyEmailWithToken(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now);
}
