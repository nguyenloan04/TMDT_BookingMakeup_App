package com.example.tmdt_bookingmakeup_app.dto.response.booking;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class BookingDto {
    private UUID id;
    private UUID customerId;
    private String customerDisplayName;
    private UUID serviceId;
    private String serviceName;
    private Double servicePrice;
    private UUID artistId;
    private String artistName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double totalAmount;
    private Double depositAmount;
    private Double platformFee;
    private String status;
}
