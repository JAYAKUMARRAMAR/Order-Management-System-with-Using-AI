package com.jayakumar.auth.repository;

import com.jayakumar.auth.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByUsername(String username);

    Optional<CustomerProfile> findByEmail(String email);
}
