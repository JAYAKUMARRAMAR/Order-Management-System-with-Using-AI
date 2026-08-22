package com.jayakumar.order.service;

import com.jayakumar.order.dto.OrderItemRequest;
import com.jayakumar.order.dto.OrderItemResponse;
import com.jayakumar.order.dto.OrderRequest;
import com.jayakumar.order.dto.OrderResponse;
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

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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

            /*
             * Temporary price.
             *
             * In the next service-to-service
             * communication step, price will be
             * retrieved from Product Service.
             */
            BigDecimal price = BigDecimal.valueOf(100);

            BigDecimal subtotal =
                    price.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.getQuantity()
                            )
                    );

            OrderItem orderItem = new OrderItem();

            orderItem.setProductId(
                    itemRequest.getProductId());

            orderItem.setQuantity(
                    itemRequest.getQuantity());

            orderItem.setPrice(price);

            orderItem.setSubtotal(subtotal);

            orderItem.setOrder(order);

            order.getItems().add(orderItem);

            totalAmount =
                    totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        /*
         * For now, order is confirmed after
         * basic order creation.
         *
         * Inventory reservation will be connected
         * in the service-to-service communication step.
         */
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