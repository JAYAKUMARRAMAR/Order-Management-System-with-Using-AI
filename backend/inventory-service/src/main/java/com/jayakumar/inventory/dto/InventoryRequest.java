package com.jayakumar.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 0, message = "Available quantity cannot be negative")
    private int availableQuantity;

    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private int reservedQuantity;
}