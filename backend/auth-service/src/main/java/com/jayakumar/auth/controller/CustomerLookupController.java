package com.jayakumar.auth.controller;

import com.jayakumar.auth.dto.CustomerLookupResponse;
import com.jayakumar.auth.repository.CustomerProfileRepository;
import jakarta.validation.constraints.Positive;
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
        return customerProfileRepository.findById(customerId)
                .map(CustomerLookupResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
