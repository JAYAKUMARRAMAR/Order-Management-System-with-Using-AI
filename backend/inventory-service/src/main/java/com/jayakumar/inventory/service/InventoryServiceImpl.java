package com.jayakumar.inventory.service;

import com.jayakumar.inventory.dto.InventoryRequest;
import com.jayakumar.inventory.dto.InventoryResponse;
import com.jayakumar.inventory.entity.Inventory;
import com.jayakumar.inventory.exception.InsufficientStockException;
import com.jayakumar.inventory.exception.InventoryNotFoundException;
import com.jayakumar.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public InventoryResponse createInventory(
            InventoryRequest request) {

        Inventory inventory = new Inventory();

        inventory.setProductId(request.getProductId());
        inventory.setAvailableQuantity(
                request.getAvailableQuantity());
        inventory.setReservedQuantity(
                request.getReservedQuantity());

        Inventory saved =
                inventoryRepository.save(inventory);

        return mapToResponse(saved);
    }

    @Override
    public List<InventoryResponse> getAllInventory() {

        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public InventoryResponse getByProductId(
            Long productId) {

        Inventory inventory =
                inventoryRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found for product: "
                                                + productId));

        return mapToResponse(inventory);
    }

    @Override
    public InventoryResponse updateInventory(
            Long productId,
            InventoryRequest request) {

        Inventory inventory =
                inventoryRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found for product: "
                                                + productId));

        inventory.setAvailableQuantity(
                request.getAvailableQuantity());

        inventory.setReservedQuantity(
                request.getReservedQuantity());

        Inventory updated =
                inventoryRepository.save(inventory);

        return mapToResponse(updated);
    }

    @Override
    public void deleteInventory(Long productId) {

        Inventory inventory =
                inventoryRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found for product: "
                                                + productId));

        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryResponse reserveStock(
            Long productId,
            int quantity) {

        Inventory inventory =
                inventoryRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found for product: "
                                                + productId));

        if (inventory.getAvailableQuantity() < quantity) {

            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + productId);
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity);

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity);

        Inventory updated =
                inventoryRepository.save(inventory);

        return mapToResponse(updated);
    }

    @Override
    public InventoryResponse releaseStock(
            Long productId,
            int quantity) {

        Inventory inventory =
                inventoryRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory not found for product: "
                                                + productId));

        if (inventory.getReservedQuantity() < quantity) {

            throw new IllegalArgumentException(
                    "Reserved stock is less than release quantity");
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity);

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity);

        Inventory updated =
                inventoryRepository.save(inventory);

        return mapToResponse(updated);
    }

    private InventoryResponse mapToResponse(
            Inventory inventory) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity()
        );
    }
}