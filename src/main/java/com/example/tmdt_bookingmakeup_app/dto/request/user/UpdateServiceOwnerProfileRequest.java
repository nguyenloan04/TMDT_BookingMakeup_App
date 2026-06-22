package com.example.tmdt_bookingmakeup_app.dto.request.user;

import com.example.tmdt_bookingmakeup_app.common.enums.ShowcaseType;

public record UpdateServiceOwnerProfileRequest(
    String bio,
    Integer experienceYears,
    ShowcaseType showcaseType,
    String identityFront,
    String identityBack
) {}
