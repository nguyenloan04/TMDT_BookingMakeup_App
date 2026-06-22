package com.example.tmdt_bookingmakeup_app.dto.response.review;

import com.example.tmdt_bookingmakeup_app.common.enums.CommentTag;
import com.example.tmdt_bookingmakeup_app.common.enums.ReviewStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class ReviewDto {
    private UUID id;
    private UUID bookingId;
    private UUID customerId;
    private String customer;
    private UUID artistId;
    private String service;
    private Integer rating;
    private Integer artistRating;
    private String comment;
    private String images;
    private CommentTag tags;
    private String date;
    private ReviewStatus status;
}
