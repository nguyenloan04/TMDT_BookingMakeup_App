package com.example.tmdt_bookingmakeup_app.dto.request.service;

public record UpdateServiceRequest(
    String name,
    String description,
    Double price,
    String category,
    Integer duration,
    Boolean isActive,
    String imageUrl
) {}
