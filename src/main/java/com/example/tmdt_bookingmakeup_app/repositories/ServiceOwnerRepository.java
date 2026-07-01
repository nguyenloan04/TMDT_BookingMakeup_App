package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOwnerRepository extends JpaRepository<ServiceOwner, UUID> {
    ServiceOwner findByUserId(UUID userId);
    List<ServiceOwner> findByVerificationStatus(String verificationStatus);
}
