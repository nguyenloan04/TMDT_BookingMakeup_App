package com.example.tmdt_bookingmakeup_app.dto.response.service;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isActive")
    private boolean isActive;
    private Double rating;
}
