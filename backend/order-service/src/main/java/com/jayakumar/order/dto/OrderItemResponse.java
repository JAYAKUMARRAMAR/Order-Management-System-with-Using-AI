package com.jayakumar.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemResponse {

    private Long productId;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}