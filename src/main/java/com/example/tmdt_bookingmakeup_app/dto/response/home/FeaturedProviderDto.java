package com.example.tmdt_bookingmakeup_app.dto.response.home;

public record FeaturedProviderDto(
        String id,  //Id from ServiceOwner
        String displayName,
        Double priceFrom,
        String avatarUrl
) {
}
