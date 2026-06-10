package com.example.tmdt_bookingmakeup_app.dto.response.artist;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArtistServiceDTO {
    private String id;
    private String name;
    private Double price;
    private Integer duration;
    private String imageUrl;
}
