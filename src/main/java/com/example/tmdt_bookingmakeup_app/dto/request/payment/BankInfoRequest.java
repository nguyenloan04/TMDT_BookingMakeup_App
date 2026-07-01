package com.example.tmdt_bookingmakeup_app.dto.request.payment;

public record BankInfoRequest(
        String bankId,
        String accountNo,
        String accountName
) {
}