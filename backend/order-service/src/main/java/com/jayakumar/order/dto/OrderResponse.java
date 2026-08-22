package com.jayakumar.order.dto;

import com.jayakumar.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {

    private Long id;

    private Long customerId;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
}