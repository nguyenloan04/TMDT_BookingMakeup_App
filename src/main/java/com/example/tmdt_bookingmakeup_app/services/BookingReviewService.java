package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.request.review.UpdateReviewStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.review.ReviewDto;
import com.example.tmdt_bookingmakeup_app.models.booking.BookingReview;
import com.example.tmdt_bookingmakeup_app.repositories.BookingReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingReviewService {

    private final BookingReviewRepository reviewRepository;

    @Autowired
    public BookingReviewService(BookingReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto updateReviewStatus(UUID id, String status) {
        BookingReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        review.setStatus(status);
        BookingReview updated = reviewRepository.save(review);
        return mapToDto(updated);
    }

    public void deleteReview(UUID id) {
        BookingReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }

    private ReviewDto mapToDto(BookingReview review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        
        if (review.getCustomer() != null) {
            dto.setCustomer(review.getCustomer().getDisplayName() != null ? 
                    review.getCustomer().getDisplayName() : review.getCustomer().getUsername());
        }
        
        if (review.getBooking() != null && review.getBooking().getService() != null) {
            dto.setService(review.getBooking().getService().getName());
        } else {
            dto.setService("Unknown Service");
        }
        
        dto.setRating(review.getBooking_rating() != null ? review.getBooking_rating() : 0);
        dto.setComment(review.getComment());
        
        if (review.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dto.setDate(review.getCreatedAt().format(formatter));
        } else {
            dto.setDate("N/A");
        }
        
        dto.setStatus(review.getStatus() != null ? review.getStatus() : "PENDING");
        return dto;
    }
}
