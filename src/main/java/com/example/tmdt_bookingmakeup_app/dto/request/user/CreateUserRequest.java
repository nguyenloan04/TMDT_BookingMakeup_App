package com.example.tmdt_bookingmakeup_app.dto.request.user;

import com.example.tmdt_bookingmakeup_app.common.enums.Gender;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;

public record CreateUserRequest(
    String username,
    String password,
    String email,
    String displayName,
    String phone,
    Gender gender,
    UserRole role
) {}
