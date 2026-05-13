package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.models.user.MakeupService;
import com.example.tmdt_bookingmakeup_app.dto.response.search.SearchResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchFilter {

    // MOCKUP DATA
    private static final List<MakeupService> MOCK_DATA = new ArrayList<>();

    static {
        MOCK_DATA.add(MakeupService.builder()
                .id(1L).title("Trang điểm cô dâu sang trọng")
                .category("Trang điểm cô dâu").categoryTag("Bridal Glam").categoryTagColor("pink")
                .artistName("Sarah Miller").artistInitials("SM").artistColor("#e91e8c")
                .rating(4.5).reviewCount(128).priceFrom(250)
                .location("Hà Nội")
                .imageUrl("https://images.unsplash.com/photo-1487412947147-5cebf100ffc2?w=400&h=300&fit=crop")
                .description("Dịch vụ trang điểm cô dâu chuyên nghiệp, phong cách sang trọng và tinh tế.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(2L).title("Phong cách thời trang sân diễn")
                .category("Thời trang / SY kiSn").categoryTag("Editorial").categoryTagColor("purple")
                .artistName("Julian Lopez").artistInitials("JL").artistColor("#9c27b0")
                .rating(4.5).reviewCount(126).priceFrom(320)
                .location("TP. Hồ Chí Minh")
                .imageUrl("https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=400&h=300&fit=crop")
                .description("Trang điểm phong cách editorial cho các buổi chụp hình thời trang và sân diễn.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(3L).title("Trang điểm cá nhân cơ bản")
                .category("Khóa học").categoryTag("Workshop").categoryTagColor("green")
                .artistName("Avery Kim").artistInitials("AK").artistColor("#4caf50")
                .rating(4.5).reviewCount(128).priceFrom(120)
                .location("Đà Nẵng")
                .imageUrl("https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=400&h=300&fit=crop")
                .description("Khóa học trang điểm cá nhân cơ bản dành cho người mới bắt đầu.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(4L).title("Trang điểm tiệc tối sang trọng")
                .category("TiSc tối").categoryTag("Evening Glam").categoryTagColor("gold")
                .artistName("Mia Tran").artistInitials("MT").artistColor("#ff9800")
                .rating(5.0).reviewCount(95).priceFrom(180)
                .location("Hà Nội")
                .imageUrl("https://images.unsplash.com/photo-1519699047748-de8e457a634e?w=400&h=300&fit=crop")
                .description("Trang điểm tiệc tối với phong cách quyến rũ và sang trọng.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(5L).title("Trang điểm cô dâu tự nhiên")
                .category("Trang điểm cô dâu").categoryTag("Bridal Glam").categoryTagColor("pink")
                .artistName("Linh Nguyen").artistInitials("LN").artistColor("#e91e8c")
                .rating(4.0).reviewCount(74).priceFrom(200)
                .location("TP. Hồ Chí Minh")
                .imageUrl("https://images.unsplash.com/photo-1560066984-138dadb4c035?w=400&h=300&fit=crop")
                .description("Trang điểm cô dâu phong cách tự nhiên, nhẹ nhàng và tinh tế.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(6L).title("Khóa học trang điểm nâng cao")
                .category("Khóa học").categoryTag("Workshop").categoryTagColor("green")
                .artistName("Hana Park").artistInitials("HP").artistColor("#009688")
                .rating(4.8).reviewCount(210).priceFrom(450)
                .location("Hà Nội")
                .imageUrl("https://images.unsplash.com/photo-1487412947147-5cebf100ffc2?w=400&h=300&fit=crop&crop=faces")
                .description("Khóa học trang điểm nâng cao dành cho những ai muốn trở thành chuyên gia.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(7L).title("Trang điểm thời trang editorial")
                .category("Thời trang / SY kiSn").categoryTag("Editorial").categoryTagColor("purple")
                .artistName("Alex Vo").artistInitials("AV").artistColor("#673ab7")
                .rating(4.3).reviewCount(88).priceFrom(280)
                .location("Đà Nẵng")
                .imageUrl("https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=400&h=300&fit=crop&crop=top")
                .description("Trang điểm editorial sáng tạo cho các dự án thời trang và nghệ thuật.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(8L).title("Trang điểm tiệc cưới hoàng gia")
                .category("Trang điểm cô dâu").categoryTag("Bridal Glam").categoryTagColor("pink")
                .artistName("Sophie Le").artistInitials("SL").artistColor("#f06292")
                .rating(5.0).reviewCount(156).priceFrom(500)
                .location("TP. Hồ Chí Minh")
                .imageUrl("https://images.unsplash.com/photo-1519699047748-de8e457a634e?w=400&h=300&fit=crop&crop=top")
                .description("Trang điểm cô dâu phong cách hoàng gia, lộng lẫy và ấn tượng.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(9L).title("Workshop trang điểm cơ bản")
                .category("Khóa học").categoryTag("Workshop").categoryTagColor("green")
                .artistName("Tom Bui").artistInitials("TB").artistColor("#26a69a")
                .rating(4.2).reviewCount(63).priceFrom(90)
                .location("Hà Nội")
                .imageUrl("https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=400&h=300&fit=crop&crop=top")
                .description("Workshop trang điểm cơ bản cuối tuần, phù hợp cho mọi lứa tuổi.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(10L).title("Trang điểm tiệc tối quyến rũ")
                .category("TiSc tối").categoryTag("Evening Glam").categoryTagColor("gold")
                .artistName("Nina Pham").artistInitials("NP").artistColor("#ff7043")
                .rating(4.7).reviewCount(112).priceFrom(220)
                .location("Đà Nẵng")
                .imageUrl("https://images.unsplash.com/photo-1487412947147-5cebf100ffc2?w=400&h=300&fit=crop&crop=entropy")
                .description("Trang điểm tiệc tối phong cách quyến rũ, phù hợp cho các sự kiện đặc biệt.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(11L).title("Trang điểm cô dâu Hàn Quốc")
                .category("Trang điểm cô dâu").categoryTag("Bridal Glam").categoryTagColor("pink")
                .artistName("Yuna Kim").artistInitials("YK").artistColor("#ec407a")
                .rating(4.9).reviewCount(189).priceFrom(350)
                .location("TP. Hồ Chí Minh")
                .imageUrl("https://images.unsplash.com/photo-1560066984-138dadb4c035?w=400&h=300&fit=crop&crop=top")
                .description("Trang điểm cô dâu phong cách Hàn Quốc, nhẹ nhàng và hiện đại.")
                .build());

        MOCK_DATA.add(MakeupService.builder()
                .id(12L).title("Trang điểm sân khấu chuyên nghiệp")
                .category("Thời trang / SY kiSn").categoryTag("Editorial").categoryTagColor("purple")
                .artistName("Marco Diaz").artistInitials("MD").artistColor("#7e57c2")
                .rating(4.6).reviewCount(97).priceFrom(400)
                .location("Hà Nội")
                .imageUrl("https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=400&h=300&fit=crop&crop=faces")
                .description("Trang điểm sân khấu chuyên nghiệp cho các buổi biểu diễn và sự kiện lớn.")
                .build());
    }

    // SEARCH LOGIC
    public SearchResponse search(String keyword, String location, String category,
                                 Double minPrice, Double maxPrice, Double minRating,
                                 String sortBy, int page, int pageSize) {

        List<MakeupService> result = MOCK_DATA.stream()
                // Filter by keyword (title, artistName, description)
                .filter(s -> keyword == null || keyword.isBlank()
                        || s.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || s.getArtistName().toLowerCase().contains(keyword.toLowerCase())
                        || s.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                // Filter by location
                .filter(s -> location == null || location.isBlank()
                        || s.getLocation().toLowerCase().contains(location.toLowerCase()))
                // Filter by category
                .filter(s -> category == null || category.isBlank()
                        || s.getCategory().equalsIgnoreCase(category))
                // Filter by price range
                .filter(s -> minPrice == null || s.getPriceFrom() >= minPrice)
                .filter(s -> maxPrice == null || s.getPriceFrom() <= maxPrice)
                // Filter by rating
                .filter(s -> minRating == null || s.getRating() >= minRating)
                .collect(Collectors.toList());

        // Sort
        if ("price_asc".equals(sortBy)) {
            result.sort(Comparator.comparingDouble(MakeupService::getPriceFrom));
        } else if ("price_desc".equals(sortBy)) {
            result.sort(Comparator.comparingDouble(MakeupService::getPriceFrom).reversed());
        } else if ("rating".equals(sortBy)) {
            result.sort(Comparator.comparingDouble(MakeupService::getRating).reversed());
        }
        // default: "suggested" - keep original order

        int totalCount = result.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int fromIndex = Math.min((page - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);
        List<MakeupService> paged = result.subList(fromIndex, toIndex);

        return SearchResponse.builder()
                .services(paged)
                .totalCount(totalCount)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }

    public List<String> getCategories() {
        return List.of("Trang điểm cô dâu", "Thời trang / Sự kiện", "Tiệc tối", "Khóa học");
    }
}
