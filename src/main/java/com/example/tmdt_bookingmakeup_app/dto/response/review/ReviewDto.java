package com.example.tmdt_bookingmakeup_app.dto.response.review;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private UUID id;
    private String customer;
    private String service;
    private Integer rating;
    private String comment;
    private String date;
    private String status;
}
