package com.jayakumar.order.client;

import com.jayakumar.order.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "inventory-service",
        url = "${inventory-service.url}",
        fallback = InventoryClientFallback.class
)
public interface InventoryClient {

    @PutMapping("/api/inventory/{productId}/reserve")
    InventoryResponse reserveStock(
            @PathVariable("productId") Long productId,
            @RequestParam("quantity") int quantity
    );
}