package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.review.CreateReviewRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.review.UpdateReviewStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.review.ReviewDto;
import com.example.tmdt_bookingmakeup_app.services.BookingReviewService;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final BookingReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ReviewController(BookingReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllReviews(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can manage reviews");
        }
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody CreateReviewRequest requestParams, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID customerId = UUID.fromString(rawUserId);
            ReviewDto created = reviewService.createReview(requestParams, customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(reviewService.getReviewsByService(serviceId));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByArtist(@PathVariable UUID artistId) {
        return ResponseEntity.ok(reviewService.getReviewsByArtist(artistId));
    }

    @GetMapping("/owner")
    public ResponseEntity<?> getMyReceivedReviews(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            return ResponseEntity.ok(reviewService.getReviewsByOwner(UUID.fromString(rawUserId)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReviewDto> updateReviewStatus(
            @PathVariable UUID id,
            @RequestBody UpdateReviewStatusRequest requestParams,
            HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reviewService.updateReviewStatus(id, requestParams.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can manage reviews");
        }
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }

    @GetMapping("/check-reviewable/{artistId}")
    public ResponseEntity<?> checkReviewable(@PathVariable UUID artistId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("canReview", false));
        }

        try {
            UUID customerId = UUID.fromString(rawUserId);
            Optional<UUID> bookingId = reviewService.getReviewableBookingId(customerId, artistId);
            return bookingId.map(uuid -> ResponseEntity.ok(Map.of(
                    "canReview", true,
                    "bookingId", uuid.toString()
            ))).orElseGet(() -> ResponseEntity.ok(Map.of("canReview", false)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("canReview", false));
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return false;
        }
        try {
            return userService.getUserProfile(UUID.fromString(rawUserId)).getRole() == UserRole.ADMIN;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
