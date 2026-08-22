package com.jayakumar.order.dto;

import lombok.Data;

@Data
public class InventoryResponse {

    private String id;

    private Long productId;

    private int availableQuantity;

    private int reservedQuantity;
}