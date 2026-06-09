package com.example.tmdt_bookingmakeup_app.dto.response.artist;

import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedArtistDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ArtistProfileResponse {
    private String ownerId;
    private String displayName;
    private String avatarUrl;
    private String address;
    private Integer experienceYears;
    private String bio;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalCustomers;
    private List<FeaturedArtistDto> artists;
    private List<ArtistServiceDTO> services;
}