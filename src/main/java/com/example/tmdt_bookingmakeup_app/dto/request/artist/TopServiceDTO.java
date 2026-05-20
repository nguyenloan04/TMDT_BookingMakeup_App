package com.example.tmdt_bookingmakeup_app.dto.request.artist;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class TopServiceDTO {
    private UUID serviceId;
    private String serviceName;
    private Long totalBookings;
}
