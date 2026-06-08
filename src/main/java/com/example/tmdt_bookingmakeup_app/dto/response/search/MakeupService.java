package com.example.tmdt_bookingmakeup_app.dto.response.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeupService {
    private Long id;
    private UUID serviceUuid;      // UUID thực của Service — dùng khi tạo booking
    private UUID artistUuid;       // UUID thực của Artist đầu tiên — dùng khi tạo booking
    private String title;
    private String category;       // "Bridal Glam", "Editorial", "Workshop", "Thời trang", "Tiệc tối", "Khóa học"
    private String artistName;
    private String artistInitials;
    private String artistColor;    // màu avatar
    private double rating;
    private int reviewCount;
    private double priceFrom;
    private Integer duration;      // thời lượng dịch vụ (phút)
    private String imageUrl;
    private String location;
    private String description;
    private String categoryTag;    // badge màu trên card
    private String categoryTagColor;
}
