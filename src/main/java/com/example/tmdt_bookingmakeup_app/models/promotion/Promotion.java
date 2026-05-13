package com.example.tmdt_bookingmakeup_app.models.promotion;

import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "promotions")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private ServiceOwner owner;

    @Column(unique = true, nullable = false)
    private String code;
    private Double discountValue;
    private Double minOrderValue;
    private Integer pointCharge;
    private LocalDateTime expiryDate;
}
