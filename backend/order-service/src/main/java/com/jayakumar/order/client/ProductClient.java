package com.jayakumar.order.client;

import com.jayakumar.order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        url = "${product-service.url}",
        fallback = ProductClientFallback.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(
            @PathVariable("id") Long id
    );
}