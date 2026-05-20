package com.example.tmdt_bookingmakeup_app.dto.request.user;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;

public record UpdateRoleRequest(
    UserRole role
) {}
