package com.example.tmdt_bookingmakeup_app.dto.response.promotion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionValidationResponse {
    private boolean valid;
    private Double discountAmount;
    private Double finalAmount;
    private String errorMessage;
}
