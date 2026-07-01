package com.example.tmdt_bookingmakeup_app.models.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.WithdrawStatus;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "withdraw_requests")
@Data
public class Withdraw {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private ServiceOwner owner;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawStatus status;

    @Column(name = "bank_account_info", columnDefinition = "TEXT")
    private String bankAccountInfo; // Lưu lại STK tại thời điểm rút (phòng khi user đổi STK sau đó)

    @Column(columnDefinition = "TEXT")
    private String note; // Lời nhắn từ Admin

    @CreationTimestamp
    private LocalDateTime createdAt;
}