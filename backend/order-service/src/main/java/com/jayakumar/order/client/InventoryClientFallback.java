package com.jayakumar.order.client;

import com.jayakumar.order.dto.InventoryResponse;
import com.jayakumar.order.exception.DownstreamServiceException;
import org.springframework.stereotype.Component;

@Component
public class InventoryClientFallback
        implements InventoryClient {

    @Override
    public InventoryResponse reserveStock(
            Long productId,
            int quantity) {

        throw new DownstreamServiceException(
                "Inventory Service is currently unavailable"
        );
    }
}