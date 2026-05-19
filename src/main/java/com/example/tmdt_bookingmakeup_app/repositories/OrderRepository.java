package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findById(UUID orderId);
}
