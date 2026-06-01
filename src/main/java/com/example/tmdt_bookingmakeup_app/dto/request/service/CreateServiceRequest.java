package com.example.tmdt_bookingmakeup_app.dto.request.service;

public record CreateServiceRequest(
    String name,
    String description,
    Double price,
    String category,
    Integer duration
) {}
