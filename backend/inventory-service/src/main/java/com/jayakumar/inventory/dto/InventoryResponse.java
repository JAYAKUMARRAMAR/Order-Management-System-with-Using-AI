package com.jayakumar.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryResponse {

    private String id;
    private Long productId;
    private int availableQuantity;
    private int reservedQuantity;
}