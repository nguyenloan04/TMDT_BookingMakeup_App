package com.example.tmdt_bookingmakeup_app.dto.response.artist;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponseDTO {
    private UUID id;
    private String artistName;
    private String specialization;
    private String portfolioImages;
    private Double averageRating;
    private Integer reviewCount;
    private Integer followCount;
    private UUID ownerId;
    private String ownerName;

    // Hàm tiện ích để chuyển đổi từ Entity sang DTO
    public static ArtistResponseDTO fromEntity(Artist artist) {
        if (artist == null) return null;

        UUID extractedOwnerId = null;
        String extractedOwnerName = null;

        if (artist.getOwner() != null && artist.getOwner().getUser() != null) {
            extractedOwnerId = artist.getOwner().getUserId();
            extractedOwnerName = artist.getOwner().getUser().getDisplayName();
        }
        return ArtistResponseDTO.builder()
                .id(artist.getId())
                .artistName(artist.getArtistName())
                .specialization(artist.getSpecialization())
                .portfolioImages(artist.getPortfolioImages())
                .averageRating(artist.getAverageRating())
                .reviewCount(artist.getReviewCount())
                .followCount(artist.getFollowCount())
                .ownerId(extractedOwnerId)
                .ownerName(extractedOwnerName)
                .build();
    }
}
