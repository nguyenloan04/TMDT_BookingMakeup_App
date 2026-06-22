package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.interaction.ServiceFavourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceFavouriteRepository extends JpaRepository<ServiceFavourite, Long> {
    List<ServiceFavourite> findByCustomerId(UUID customerId);
    Optional<ServiceFavourite> findByCustomerIdAndServiceId(UUID customerId, UUID serviceId);
    boolean existsByCustomerIdAndServiceId(UUID customerId, UUID serviceId);
}
