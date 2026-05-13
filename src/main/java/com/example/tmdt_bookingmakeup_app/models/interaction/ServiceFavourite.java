package com.example.tmdt_bookingmakeup_app.models.interaction;

import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "service_favourites")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFavourite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
}
