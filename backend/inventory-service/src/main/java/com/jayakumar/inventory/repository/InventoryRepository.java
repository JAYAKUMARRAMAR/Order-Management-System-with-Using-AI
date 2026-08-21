package com.jayakumar.inventory.repository;

import com.jayakumar.inventory.entity.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InventoryRepository
        extends MongoRepository<Inventory, String> {

    Optional<Inventory> findByProductId(Long productId);
}