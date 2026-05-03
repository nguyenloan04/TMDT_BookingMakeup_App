package com.example.tmdt_bookingmakeup_app.dto.response.auth;

public record AuthResponse(
        boolean result,
        String message,
        AuthDto authDto
) { }
