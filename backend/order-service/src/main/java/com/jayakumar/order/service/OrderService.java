package com.jayakumar.order.service;

import com.jayakumar.order.dto.OrderRequest;
import com.jayakumar.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    void cancelOrder(Long id);
}