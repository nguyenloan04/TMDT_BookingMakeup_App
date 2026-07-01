package com.example.tmdt_bookingmakeup_app.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponse {
    private Double totalRevenue;
    private Double platformFee;
    private Double studioRevenue;
    private Double totalDeposit;
    private List<BookingDetailDto> bookings;
}
