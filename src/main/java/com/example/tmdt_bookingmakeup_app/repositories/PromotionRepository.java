package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.promotion.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByCode(String code);
    List<Promotion> findByOwnerUserId(UUID ownerUserId);
    List<Promotion> findAllByExpiryDateAfter(LocalDateTime date);
    List<Promotion> findAllByOwnerUserIdAndExpiryDateAfter(UUID ownerUserId, LocalDateTime date);
}
