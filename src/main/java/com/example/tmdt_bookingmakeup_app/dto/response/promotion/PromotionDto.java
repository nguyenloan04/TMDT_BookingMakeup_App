package com.example.tmdt_bookingmakeup_app.dto.response.promotion;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PromotionDto {
    private UUID id;
    private UUID ownerId;
    private String code;
    private Double discountValue;
    private Double minOrderValue;
    private Integer pointCharge;
    private LocalDateTime expiryDate;
}
