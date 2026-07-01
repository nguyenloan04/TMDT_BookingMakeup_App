package com.example.tmdt_bookingmakeup_app.dto.response.payment;

import java.util.UUID;

public record WalletResponse(
        UUID id,
        Double balance,
        String bankId,
        String accountNo,
        String accountName,
        String message
) {
}