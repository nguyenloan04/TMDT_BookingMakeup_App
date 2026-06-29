package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.response.artist.ArtistServiceDTO;
import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedArtistDto;
import com.example.tmdt_bookingmakeup_app.dto.response.artist.ProviderProfileResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.artist.ArtistServiceDetailResponse;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.ArtistRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceRepository;
import com.example.tmdt_bookingmakeup_app.services.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile/providers")
@RequiredArgsConstructor
public class ServiceProviderController {

    private final ArtistRepository artistRepository;
    private final ServiceRepository serviceRepository;
    private final HomePageService homePageService;
    private final ServiceOwnerRepository serviceOwnerRepository;

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderProfileResponse> getProviderProfile(@PathVariable UUID providerId) {
        ServiceOwner currentProvider = serviceOwnerRepository.findById(providerId).orElse(null);
        if (currentProvider == null) {
            return ResponseEntity.status(404).body(null);
        }

        UUID ownerId = currentProvider.getUserId();

        List<Artist> artists = artistRepository.findByOwnerUserId(ownerId);
        List<Service> services = serviceRepository.findByOwnerUserIdAndIsActiveTrue(ownerId);

        List<FeaturedArtistDto> artistDtos = artists.stream()
                .map(homePageService::mapToFeaturedArtistDto)
                .toList();

        List<ArtistServiceDTO> serviceDtos = services.stream().map(s ->
                ArtistServiceDTO.builder()
                        .id(s.getId().toString())
                        .name(s.getName())
                        .price(s.getPrice())
                        .duration(s.getDuration() != null ? s.getDuration() : 60)
                        .imageUrl(null)
                        .build()
        ).collect(Collectors.toList());

        double rating = artistDtos.stream().mapToDouble(FeaturedArtistDto::rating).average().orElse(0.0);
        int reviewCount = artistDtos.stream().mapToInt(FeaturedArtistDto::reviewsCount).sum();

        ProviderProfileResponse response = ProviderProfileResponse.builder()
                .ownerId(ownerId.toString())
                .displayName(currentProvider.getUser() != null ? currentProvider.getUser().getDisplayName() : "")
                .avatarUrl(currentProvider.getUser() != null ? currentProvider.getUser().getAvatarUrl() : null)
                .address(currentProvider.getUser() != null ? currentProvider.getUser().getAddress() : "TP. Hồ Chí Minh")
                .experienceYears(currentProvider.getExperienceYears() != null ? currentProvider.getExperienceYears() : 5)
                .bio(currentProvider.getBio())
                .averageRating(rating)
                .totalReviews(reviewCount)
                .artists(artistDtos)
                .services(serviceDtos)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ArtistServiceDetailResponse> getServiceDetail(@PathVariable UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));

        Artist artists = artistRepository.findByOwnerUserId(service.getOwner().getUserId()).stream().findFirst().orElse(null);

        UUID ownerId = service.getOwner().getUserId();
        List<Service> related = serviceRepository.findTop3ByOwnerUserIdAndIdNotAndIsActiveTrue(ownerId, serviceId);

        List<ArtistServiceDTO> relatedDtos = related.stream().map(s ->
                ArtistServiceDTO.builder()
                        .id(s.getId().toString())
                        .name(s.getName())
                        .price(s.getPrice())
                        .duration(s.getDuration() != null ? s.getDuration() : 60)
                        .imageUrl(null)
                        .build()
        ).collect(Collectors.toList());

        User owner = service.getOwner().getUser();
        ArtistServiceDetailResponse response = ArtistServiceDetailResponse.builder()
                .serviceId(service.getId().toString())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .duration(service.getDuration() != null ? service.getDuration() : 60)
                .category(service.getCategory())
                .ownerId(ownerId.toString())
                .ownerName(owner != null ? owner.getDisplayName() : "Studio")
                .ownerAvatar(owner != null ? owner.getAvatarUrl() : null)
                .relatedServices(relatedDtos)
                .mainThumbnailUrl(artists != null ? artists.getPortfolioImages() : null)
                .rating(artists != null ? artists.getAverageRating() : 0)
                .reviewCount(artists != null ? artists.getReviewCount() : 0)
                .address(owner != null ? owner.getAddress() : "")
                .build();

        return ResponseEntity.ok(response);
    }
}