package com.example.tmdt_bookingmakeup_app.models.orders;

import com.example.tmdt_bookingmakeup_app.common.enums.OrderStatus;
import com.example.tmdt_bookingmakeup_app.common.enums.PaymentStatus;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private User artist;

    @ManyToOne(fetch = FetchType.LAZY)
    private Service makeupService;

    private LocalDateTime bookingDate;
    private String address;
    private String note;

    private long totalPrice;
    private long depositAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
