package com.jayakumar.inventory.service;

import com.jayakumar.inventory.dto.InventoryRequest;
import com.jayakumar.inventory.dto.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse createInventory(
            InventoryRequest request);

    List<InventoryResponse> getAllInventory();

    InventoryResponse getByProductId(Long productId);

    InventoryResponse updateInventory(
            Long productId,
            InventoryRequest request);

    void deleteInventory(Long productId);

    InventoryResponse reserveStock(
            Long productId,
            int quantity);

    InventoryResponse releaseStock(
            Long productId,
            int quantity);
}