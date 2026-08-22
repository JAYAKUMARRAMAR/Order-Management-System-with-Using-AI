package com.jayakumar.order.controller;

import com.jayakumar.order.dto.OrderRequest;
import com.jayakumar.order.dto.OrderResponse;
import com.jayakumar.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody OrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {

        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable Long id) {

        return orderService.getOrderById(id);
    }

    @PutMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(
            @PathVariable Long id) {

        orderService.cancelOrder(id);
    }
}