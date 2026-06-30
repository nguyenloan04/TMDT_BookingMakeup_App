package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.payment.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByOwnerId(UUID ownerId);
}
