package com.example.tmdt_bookingmakeup_app.dto.request.auth;

public record RegisterRequest(
        String email,
        String username,
        String password
) {
}
