package com.example.tmdt_bookingmakeup_app.dto.response.home;

public record HomePromotionDto (
     String id,
     String code,
     Double discountValue,
     String title,
     String validUntil
) {}