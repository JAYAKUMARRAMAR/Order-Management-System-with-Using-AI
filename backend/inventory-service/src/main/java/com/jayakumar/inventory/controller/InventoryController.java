package com.jayakumar.inventory.controller;

import com.jayakumar.inventory.dto.InventoryRequest;
import com.jayakumar.inventory.dto.InventoryResponse;
import com.jayakumar.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(
            @Valid @RequestBody InventoryRequest request) {

        return inventoryService.createInventory(request);
    }

    @GetMapping
    public List<InventoryResponse> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{productId}")
    public InventoryResponse getByProductId(
            @PathVariable Long productId) {

        return inventoryService.getByProductId(productId);
    }

    @PutMapping("/{productId}")
    public InventoryResponse updateInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryRequest request) {

        return inventoryService.updateInventory(
                productId,
                request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(
            @PathVariable Long productId) {

        inventoryService.deleteInventory(productId);
    }

    @PutMapping("/{productId}/reserve")
    public InventoryResponse reserveStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return inventoryService.reserveStock(
                productId,
                quantity);
    }

    @PutMapping("/{productId}/release")
    public InventoryResponse releaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return inventoryService.releaseStock(
                productId,
                quantity);
    }
}