package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedArtistDto;
import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedProviderDto;
import com.example.tmdt_bookingmakeup_app.dto.response.home.HomePageResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.home.HomePromotionDto;
import com.example.tmdt_bookingmakeup_app.services.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomePageController {

    private final HomePageService homePageService;

    @GetMapping("/data")
    public ResponseEntity<HomePageResponse> getHomePageData() {
        HomePageResponse response = homePageService.getHomePageData();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured-providers")
    public ResponseEntity<List<FeaturedProviderDto>> getFeaturedProviders() {
        return ResponseEntity.ok(homePageService.getTopProviders());
    }

    @GetMapping("/featured-artists")
    public ResponseEntity<List<FeaturedArtistDto>> getFeaturedArtists() {
        return ResponseEntity.ok(homePageService.getTopArtists());
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<HomePromotionDto>> getPromotions() {
        return ResponseEntity.ok(homePageService.getActivePromotions());
    }
}