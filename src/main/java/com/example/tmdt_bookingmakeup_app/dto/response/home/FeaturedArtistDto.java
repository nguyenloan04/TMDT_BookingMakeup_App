package com.example.tmdt_bookingmakeup_app.dto.response.home;

public record FeaturedArtistDto (
     String id,
     String displayName,
     String specialty,
     Double rating,
     Integer reviewsCount,
     Double priceFrom,
     String avatarUrl
) {}