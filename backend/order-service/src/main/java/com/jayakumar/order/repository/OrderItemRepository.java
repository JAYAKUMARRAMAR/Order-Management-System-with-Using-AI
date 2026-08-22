package com.jayakumar.order.repository;

import com.jayakumar.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}