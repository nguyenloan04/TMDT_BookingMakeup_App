package com.example.tmdt_bookingmakeup_app.dto.request.user;

public record ChangePasswordRequest(
    String oldPassword,
    String newPassword
) {}
