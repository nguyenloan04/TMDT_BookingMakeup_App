package com.example.tmdt_bookingmakeup_app.dto.request.promotion;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePromotionRequest(
    UUID ownerId,
    String code,
    Double discountValue,
    Double minOrderValue,
    Integer pointCharge,
    LocalDateTime expiryDate
) {}
