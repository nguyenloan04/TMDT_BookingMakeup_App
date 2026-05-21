package com.example.tmdt_bookingmakeup_app.dto.request.user;

import com.example.tmdt_bookingmakeup_app.common.enums.Gender;
import java.time.LocalDateTime;

public record UpdateProfileRequest(
    String displayName,
    String avatarUrl,
    String phone,
    Gender gender,
    LocalDateTime address
) {}
