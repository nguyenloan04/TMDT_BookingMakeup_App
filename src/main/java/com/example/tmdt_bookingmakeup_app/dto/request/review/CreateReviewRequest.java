package com.example.tmdt_bookingmakeup_app.dto.request.review;

import com.example.tmdt_bookingmakeup_app.common.enums.CommentTag;
import java.util.UUID;

public record CreateReviewRequest(
    UUID bookingId,
    Integer bookingRating,
    Integer artistRating,
    String comment,
    String images,
    CommentTag tags
) {}
