package com.example.tmdt_bookingmakeup_app.dto.request.promotion;

import java.util.UUID;

public record ValidatePromotionRequest(
    String code,
    Double bookingAmount,
    UUID ownerId
) {}
