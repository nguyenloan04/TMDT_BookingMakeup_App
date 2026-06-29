package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedArtistDto;
import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedProviderDto;
import com.example.tmdt_bookingmakeup_app.dto.response.home.HomePageResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.home.HomePromotionDto;
import com.example.tmdt_bookingmakeup_app.models.promotion.Promotion;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.ArtistRepository;
import com.example.tmdt_bookingmakeup_app.repositories.PromotionRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageService {

    private final ArtistRepository artistRepository;
    private final PromotionRepository promotionRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;

    public HomePageResponse getHomePageData() {
        List<FeaturedProviderDto> featuredProviders = getTopProviders();
        List<FeaturedArtistDto> featuredArtists = getTopArtists();
        List<HomePromotionDto> promotions = getActivePromotions();

        return new HomePageResponse(featuredProviders, featuredArtists, promotions);
    }

    private List<FeaturedProviderDto> getTopProviders() {
        List<ServiceOwner> topProviders = artistRepository.findByOwner_User_RoleNot(UserRole.ADMIN,
                PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "averageRating"))
        ).getContent().stream().map(Artist::getOwner).toList();

        return topProviders.stream()
                .map(this::mapToFeaturedProviderDto)
                .toList();
    }

    private List<FeaturedArtistDto> getTopArtists() {
        List<Artist> topArtists = artistRepository.findByOwner_User_RoleNot(UserRole.ADMIN,
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "averageRating"))
        ).getContent();

        return topArtists.stream()
                .map(this::mapToFeaturedArtistDto)
                .toList();
    }

    private List<HomePromotionDto> getActivePromotions() {
        List<Promotion> activePromos = promotionRepository.findAllByExpiryDateAfter(LocalDateTime.now());
        return activePromos.stream()
                .map(this::mapToHomePromotionDto)
                .collect(Collectors.toList());
    }

    public FeaturedProviderDto mapToFeaturedProviderDto(ServiceOwner serviceOwner) {
        Double fetchedPrice = serviceRepository.findMinPriceByOwnerId(serviceOwner.getUserId());
        double minPrice = fetchedPrice != null ? fetchedPrice : 0.0;

        String avatarUrl = serviceOwner.getUser().getAvatarUrl();

        String displayName = serviceOwner.getUser().getDisplayName() != null
                ? serviceOwner.getUser().getDisplayName()
                : serviceOwner.getUser().getUsername();
        return new FeaturedProviderDto(
                serviceOwner.getUserId().toString(),
                displayName,
                minPrice,
                avatarUrl
        );
    }

    public FeaturedArtistDto mapToFeaturedArtistDto(Artist a) {
        Double minPrice = 0.0;
        if (a.getOwner() != null && a.getOwner().getUserId() != null) {
            Double fetchedPrice = serviceRepository.findMinPriceByOwnerId(a.getOwner().getUserId());
            minPrice = fetchedPrice != null ? fetchedPrice : 0.0;
        }

        String avatarUrl = null;
        if (a.getPortfolioImages() != null && !a.getPortfolioImages().isBlank()) {
            avatarUrl = a.getPortfolioImages().split(",")[0];
        }

        return new FeaturedArtistDto(
                a.getId().toString(),
                a.getArtistName(),
                a.getSpecialization(),
                a.getAverageRating() != null ? a.getAverageRating() : 0.0,
                a.getReviewCount() != null ? a.getReviewCount() : 0,
                minPrice,
                avatarUrl
        );
    }

    private HomePromotionDto mapToHomePromotionDto(Promotion p) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String title = "Giảm " + String.format("%,.0f", p.getDiscountValue()) + "đ";
        String validUntil = p.getExpiryDate() != null ? p.getExpiryDate().format(formatter) : "";

        return new HomePromotionDto(
                p.getId().toString(),
                p.getCode(),
                p.getDiscountValue(),
                title,
                validUntil
        );
    }
}