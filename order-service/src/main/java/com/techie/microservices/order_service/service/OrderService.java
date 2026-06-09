package com.techie.microservices.order_service.service;

import com.techie.microservices.order_service.client.InventoryClient;
import com.techie.microservices.order_service.dto.OrderRequest;
import com.techie.microservices.order_service.model.Order;
import com.techie.microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; 

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest) {
        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(),orderRequest.quantity());

    if(isProductInStock) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setSkuCode(orderRequest.skuCode());
        order.setPrice(orderRequest.price());
        order.setQuantity(orderRequest.quantity());

        orderRepository.save(order);
       // log.info("Order placed successfully with order number: {}", order.getOrderNumber());
    }
    else{
        throw new RuntimeException("Product with SKU code " + orderRequest.skuCode() + " is not in stock.");
    }
    }
}

