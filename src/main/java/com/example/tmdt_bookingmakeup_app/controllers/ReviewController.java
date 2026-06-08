package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.request.review.UpdateReviewStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.review.ReviewDto;
import com.example.tmdt_bookingmakeup_app.services.BookingReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final BookingReviewService reviewService;

    @Autowired
    public ReviewController(BookingReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReviewDto> updateReviewStatus(
            @PathVariable UUID id, 
            @RequestBody UpdateReviewStatusRequest request) {
        return ResponseEntity.ok(reviewService.updateReviewStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
