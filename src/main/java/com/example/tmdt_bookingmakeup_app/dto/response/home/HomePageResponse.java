package com.example.tmdt_bookingmakeup_app.dto.response.home;

import java.util.List;

public record HomePageResponse(
        List<FeaturedProviderDto> featuredProviders,
        List<FeaturedArtistDto> featuredArtists,
        List<HomePromotionDto> promotions
) {
}