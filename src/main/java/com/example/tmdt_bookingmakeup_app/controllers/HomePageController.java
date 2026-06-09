package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.response.home.HomePageResponse;
import com.example.tmdt_bookingmakeup_app.services.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}