package com.example.tmdt_bookingmakeup_app.dto.response.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeupService {
    private Long id;
    private String title;
    private String category;       // "Bridal Glam", "Editorial", "Workshop", "Thời trang", "TiSc tối", "Khóa học"
    private String artistName;
    private String artistInitials;
    private String artistColor;    // màu avatar
    private double rating;
    private int reviewCount;
    private double priceFrom;
    private String imageUrl;
    private String location;
    private String description;
    private String categoryTag;    // badge màu trên card
    private String categoryTagColor;
}
