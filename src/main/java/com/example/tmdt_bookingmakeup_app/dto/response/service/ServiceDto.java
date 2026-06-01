package com.example.tmdt_bookingmakeup_app.dto.response.service;

import lombok.Data;
import java.util.UUID;

@Data
public class ServiceDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer duration;
    private boolean isActive;
    private Double rating;
}
