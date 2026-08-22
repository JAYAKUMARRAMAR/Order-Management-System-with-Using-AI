package com.jayakumar.order.client;

import com.jayakumar.order.dto.ProductResponse;
import com.jayakumar.order.exception.DownstreamServiceException;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback
        implements ProductClient {

    @Override
    public ProductResponse getProductById(Long id) {

        throw new DownstreamServiceException(
                "Product Service is currently unavailable"
        );
    }
}