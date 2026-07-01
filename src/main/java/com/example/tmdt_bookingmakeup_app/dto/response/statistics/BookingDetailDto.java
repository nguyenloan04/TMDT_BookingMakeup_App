package com.example.tmdt_bookingmakeup_app.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailDto {
    private UUID id;
    private LocalDate bookingDate;
    private String customerName;
    private String serviceName;
    private Double totalAmount;
    private Double depositAmount;
    private Double platformFee;
    private Double studioReceives;
    private String status;
    private String statusLabel;
}
