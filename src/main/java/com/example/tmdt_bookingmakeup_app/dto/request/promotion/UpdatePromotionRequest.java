package com.example.tmdt_bookingmakeup_app.dto.request.promotion;

import java.time.LocalDateTime;

public record UpdatePromotionRequest(
    Double discountValue,
    Double minOrderValue,
    Integer pointCharge,
    LocalDateTime expiryDate
) {}
