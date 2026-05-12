package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.response.search.SearchResponse;
import com.example.tmdt_bookingmakeup_app.services.SearchFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class SearchFilterController {

    private final SearchFilter searchService;

    public SearchFilterController(SearchFilter searchService) {
        this.searchService = searchService;
    }

    /**
     * Search services with filters
     * GET /api/services/search?keyword=&location=&category=&minPrice=&maxPrice=&minRating=&sortBy=&page=1&pageSize=6
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "suggested") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize
    ) {
        SearchResponse response = searchService.search(
                keyword, location, category, minPrice, maxPrice, minRating, sortBy, page, pageSize
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all available categories
     * GET /api/services/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(searchService.getCategories());
    }
}
