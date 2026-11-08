package com.jayakumar.auth.controller;

import com.jayakumar.auth.dto.CustomerLookupResponse;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/customers")
@Validated
public class CustomerLookupController {

    private final CustomerProfileRepository customerProfileRepository;

    public CustomerLookupController(CustomerProfileRepository customerProfileRepository) {
        this.customerProfileRepository = customerProfileRepository;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerLookupResponse> getCustomer(
            @PathVariable @Positive Long customerId) {
        var profile = customerProfileRepository.findById(customerId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var customer = profile.get();
        if (!customer.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .<CustomerLookupResponse>build();
        }

        return ResponseEntity.ok(CustomerLookupResponse.from(customer));
    }
}
