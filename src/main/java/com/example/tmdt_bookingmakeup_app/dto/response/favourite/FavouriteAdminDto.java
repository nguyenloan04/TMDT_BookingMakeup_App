package com.example.tmdt_bookingmakeup_app.dto.response.favourite;

import java.util.UUID;

public record FavouriteAdminDto(
    Long id,
    UUID customerId,
    String customerName,
    String customerEmail,
    UUID serviceId,
    String serviceName,
    Double servicePrice,
    String artistName
) {}
