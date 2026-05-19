package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.OrderStatus;
import com.example.tmdt_bookingmakeup_app.models.orders.Order;
import com.example.tmdt_bookingmakeup_app.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Transactional
    public void updateStatus(UUID orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }
}
