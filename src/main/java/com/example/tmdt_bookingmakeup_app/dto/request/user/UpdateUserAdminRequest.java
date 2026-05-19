package com.example.tmdt_bookingmakeup_app.dto.request.user;

import com.example.tmdt_bookingmakeup_app.common.enums.Gender;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import java.time.LocalDateTime;

public record UpdateUserAdminRequest(
    String displayName,
    String avatarUrl,
    String phone,
    Gender gender,
    UserRole role,
    Boolean active,
    LocalDateTime address
) {}
