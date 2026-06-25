package com.example.tmdt_bookingmakeup_app.dto.request.auth;

import com.example.tmdt_bookingmakeup_app.common.enums.ShowcaseType;

public record RegisterServiceOwnerRequest(
        String email,
        String username,
        String password,
        String bio,
        Integer experienceYears,
        ShowcaseType showcaseType,
        String identityFront,
        String identityBack
) {
}
