package com.example.tmdt_bookingmakeup_app.dto.request.review;

import com.example.tmdt_bookingmakeup_app.common.enums.ReviewStatus;

public record UpdateReviewStatusRequest(
    ReviewStatus status
) {}
