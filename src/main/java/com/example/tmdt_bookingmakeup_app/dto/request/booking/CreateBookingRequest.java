package com.example.tmdt_bookingmakeup_app.dto.request.booking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateBookingRequest(
    UUID serviceId,
    UUID artistId,
    LocalDate bookingDate,
    LocalTime startTime,
    String promoCode
) {}
