package com.example.tmdt_bookingmakeup_app.models.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.PaymentStatus;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "payments")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    @Column(name="transaction_type")
    private String transactionType; // deposit
    @Column(name="method")
    private String paymentMethod; // Momo, ATM...
    @Column(name="total_amount")
    private Double amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "TEXT")
    private PaymentStatus status;
}
