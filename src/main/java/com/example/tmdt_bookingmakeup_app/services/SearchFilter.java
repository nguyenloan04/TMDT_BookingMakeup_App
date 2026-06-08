package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.response.search.SearchResponse;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.dto.response.search.MakeupService;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.ArtistRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class SearchFilter {

    // Màu avatar theo index (xoay vòng)
    private static final String[] AVATAR_COLORS = {
            "#e91e8c", "#9c27b0", "#4caf50", "#ff9800",
            "#673ab7", "#f06292", "#009688", "#26a69a",
            "#ff7043", "#ec407a", "#7e57c2", "#00bcd4"
    };

    // Map category → tag + màu
    private static final Map<String, String[]> CATEGORY_TAG_MAP = Map.of(
            "trang điểm cô dâu",    new String[]{"Bridal Glam", "pink"},
            "thời trang / sự kiện", new String[]{"Editorial", "purple"},
            "tiệc tối",             new String[]{"Evening Glam", "gold"},
            "khóa học",             new String[]{"Workshop", "green"}
    );

    private final ServiceRepository serviceRepository;
    private final ArtistRepository artistRepository;

    public SearchFilter(ServiceRepository serviceRepository, ArtistRepository artistRepository) {
        this.serviceRepository = serviceRepository;
        this.artistRepository = artistRepository;
    }

    public SearchResponse search(String keyword, String location, String category,
                                 Double minPrice, Double maxPrice, Double minRating,
                                 String sortBy, int page, int pageSize) {

        // Native query không hỗ trợ Sort động → dùng PageRequest không sort,
        // sau đó sort ở Java layer (dữ liệu đã được filter, số lượng nhỏ)
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // Chuẩn hóa params: null nếu blank
        String kw  = (keyword  != null && !keyword.isBlank())  ? keyword.trim()  : null;
        String loc = (location != null && !location.isBlank()) ? location.trim() : null;
        String cat = (category != null && !category.isBlank()) ? category.trim() : null;

        Page<Service> resultPage = serviceRepository.searchServices(
                kw, loc, cat, minPrice, maxPrice, minRating, pageable
        );

        List<MakeupService> services = resultPage.getContent().stream()
                .map(this::toMakeupService)
                .toList();

        // Sort sau khi map
        String effectiveSortBy = sortBy == null ? "suggested" : sortBy;
        services = switch (effectiveSortBy) {
            case "price_asc"  -> services.stream()
                    .sorted((a, b) -> Double.compare(a.getPriceFrom(), b.getPriceFrom()))
                    .toList();
            case "price_desc" -> services.stream()
                    .sorted((a, b) -> Double.compare(b.getPriceFrom(), a.getPriceFrom()))
                    .toList();
            case "rating"     -> services.stream()
                    .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                    .toList();
            default           -> services; // "suggested" - giữ nguyên thứ tự DB
        };

        return SearchResponse.builder()
                .services(services)
                .totalCount((int) resultPage.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .totalPages(resultPage.getTotalPages())
                .build();
    }

    public List<String> getCategories() {
        return List.of("Trang điểm cô dâu", "Thời trang / Sự kiện", "Tiệc tối", "Khóa học");
    }

    // Mapper

    private MakeupService toMakeupService(Service service) {
        ServiceOwner owner = service.getOwner();

        // Lấy thông tin artist (lấy cái đầu tiên nếu có nhiều)
        List<Artist> artists = artistRepository.findByOwnerUserId(owner.getUserId());
        Artist artist = artists.isEmpty() ? null : artists.get(0);

        String artistName;
        double rating = 0.0;
        int reviewCount = 0;

        if (artist != null) {
            artistName  = artist.getArtistName();
            rating      = artist.getAverageRating() != null ? artist.getAverageRating() : 0.0;
            reviewCount = artist.getReviewCount()   != null ? artist.getReviewCount()   : 0;
        } else {
            // Fallback: dùng display_name của user
            artistName = owner.getUser().getDisplayName() != null
                    ? owner.getUser().getDisplayName()
                    : owner.getUser().getUsername();
        }

        String artistInitials = buildInitials(artistName);
        String artistColor    = AVATAR_COLORS[Math.abs(artistName.hashCode()) % AVATAR_COLORS.length];

        // Category tag
        String[] tagInfo = CATEGORY_TAG_MAP.getOrDefault(
                service.getCategory() != null ? service.getCategory().toLowerCase() : "",
                new String[]{service.getCategory() != null ? service.getCategory() : "Khác", "pink"}
        );

        // Image: dùng portfolioImages của artist nếu có
        String imageUrl = (artist != null && artist.getPortfolioImages() != null && !artist.getPortfolioImages().isBlank())
                ? artist.getPortfolioImages()
                : "https://images.unsplash.com/photo-1487412947147-5cebf100ffc2?w=400&h=300&fit=crop";

        // Location: lấy từ address của user
        String location = owner.getUser().getAddress() != null
                ? owner.getUser().getAddress()
                : "";

        return MakeupService.builder()
                .id(service.getId() != null ? service.getId().getMostSignificantBits() & Long.MAX_VALUE : 0L)
                .serviceUuid(service.getId())
                .artistUuid(artist != null ? artist.getId() : null)
                .title(service.getName())
                .category(service.getCategory())
                .categoryTag(tagInfo[0])
                .categoryTagColor(tagInfo[1])
                .artistName(artistName)
                .artistInitials(artistInitials)
                .artistColor(artistColor)
                .rating(rating)
                .reviewCount(reviewCount)
                .priceFrom(service.getPrice() != null ? service.getPrice() : 0.0)
                .duration(service.getDuration())
                .imageUrl(imageUrl)
                .location(location)
                .description(service.getDescription() != null ? service.getDescription() : "")
                .build();
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
