package com.example.tmdt_bookingmakeup_app.dto.request.auth;

public record VerifyRequest(
        String email,
        String code,
        String newPassword
) {
}
