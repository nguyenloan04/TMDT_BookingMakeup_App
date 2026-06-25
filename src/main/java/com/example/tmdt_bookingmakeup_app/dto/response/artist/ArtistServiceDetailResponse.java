package com.example.tmdt_bookingmakeup_app.dto.response.artist;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ArtistServiceDetailResponse {
    private String serviceId;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private String category;
    private String ownerId;
    private String ownerName;
    private String ownerAvatar;
    private List<ArtistServiceDTO> relatedServices;
    private String mainThumbnailUrl;
    private double rating;
    private int reviewCount;
    private String address;
}

