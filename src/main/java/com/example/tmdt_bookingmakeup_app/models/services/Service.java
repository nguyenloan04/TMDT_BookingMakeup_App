package com.example.tmdt_bookingmakeup_app.models.services;

import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "services")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private ServiceOwner owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    private String category;

    private Integer duration;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "rating")
    private Double rating = 0.0;
}
