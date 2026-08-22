package com.jayakumar.order.service;

import com.jayakumar.order.client.InventoryClient;
import com.jayakumar.order.client.ProductClient;
import com.jayakumar.order.dto.OrderItemRequest;
import com.jayakumar.order.dto.OrderItemResponse;
import com.jayakumar.order.dto.OrderRequest;
import com.jayakumar.order.dto.OrderResponse;
import com.jayakumar.order.dto.ProductResponse;
import com.jayakumar.order.entity.Order;
import com.jayakumar.order.entity.OrderItem;
import com.jayakumar.order.entity.OrderStatus;
import com.jayakumar.order.exception.OrderNotFoundException;
import com.jayakumar.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderServiceImpl(
        OrderRepository orderRepository,
        ProductClient productClient,
        InventoryClient inventoryClient) {

    this.orderRepository = orderRepository;
    this.productClient = productClient;
    this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

    Order order = new Order();

    order.setCustomerId(request.getCustomerId());
    order.setStatus(OrderStatus.PENDING);
    order.setCreatedAt(LocalDateTime.now());

    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItemRequest itemRequest : request.getItems()) {

        // 1. Get real product information
        ProductResponse product =
                productClient.getProductById(
                        itemRequest.getProductId()
                );

        // 2. Get actual product price
        BigDecimal price = product.getPrice();

        // 3. Reserve stock
        inventoryClient.reserveStock(
                itemRequest.getProductId(),
                itemRequest.getQuantity()
        );

        // 4. Calculate subtotal
        BigDecimal subtotal =
                price.multiply(
                        BigDecimal.valueOf(
                                itemRequest.getQuantity()
                        )
                );

        // 5. Create OrderItem
        OrderItem orderItem = new OrderItem();

        orderItem.setProductId(
                itemRequest.getProductId()
        );

        orderItem.setQuantity(
                itemRequest.getQuantity()
        );

        orderItem.setPrice(price);

        orderItem.setSubtotal(subtotal);

        orderItem.setOrder(order);

        order.getItems().add(orderItem);

        // 6. Calculate total
        totalAmount =
                totalAmount.add(subtotal);
    }

    order.setTotalAmount(totalAmount);

    // 7. Order confirmed
    order.setStatus(OrderStatus.CONFIRMED);

    Order savedOrder =
            orderRepository.save(order);

    return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {

        Order order =
                orderRepository.findById(id)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with id: "
                                                + id));

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {

        Order order =
                orderRepository.findById(id)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with id: "
                                                + id));

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getProductId(),
                                        item.getQuantity(),
                                        item.getPrice(),
                                        item.getSubtotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}