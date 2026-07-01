package com.example.tmdt_bookingmakeup_app.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatisticsResponse {
    private Long totalBookings;
    private Long pendingCount;
    private Long confirmedCount;
    private Long paidCount;
    private Long completedCount;
    private Long cancelledCount;
    private Long customerCount;

    // Percentages for status distribution
    private Double pendingPercentage;
    private Double confirmedPercentage;
    private Double paidPercentage;
    private Double completedPercentage;
    private Double cancelledPercentage;

    // Conversion and completion rates
    private Double completionRate;
    private Double cancellationRate;
}
