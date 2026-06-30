package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.common.enums.ReviewStatus;
import com.example.tmdt_bookingmakeup_app.dto.request.review.CreateReviewRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.review.ReviewDto;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.booking.BookingReview;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.repositories.ArtistRepository;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import com.example.tmdt_bookingmakeup_app.repositories.BookingReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingReviewService {

    private final BookingReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public BookingReviewService(
            BookingReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            ArtistRepository artistRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.artistRepository = artistRepository;
    }

    public Optional<UUID> getReviewableBookingId(UUID customerId, UUID artistId) {
        List<Booking> completedBookings = bookingRepository
                .findByCustomerIdAndArtistIdAndStatusOrderByBookingDateDesc(customerId, artistId, BookingStatus.COMPLETED);
        return completedBookings.stream()
                .filter(b -> !reviewRepository.existsByBookingId(b.getId()))
                .map(Booking::getId)
                .findFirst();
    }

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto updateReviewStatus(UUID id, ReviewStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Review status is required");
        }
        BookingReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        review.setStatus(status);
        BookingReview updated = reviewRepository.save(review);
        refreshArtistRating(updated.getArtist());
        return mapToDto(updated);
    }

    @Transactional
    public void deleteReview(UUID id) {
        BookingReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        Artist artist = review.getArtist();
        reviewRepository.delete(review);
        reviewRepository.flush();
        refreshArtistRating(artist);
    }

    private ReviewDto mapToDto(BookingReview review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());

        if (review.getBooking() != null) {
            dto.setBookingId(review.getBooking().getId());
        }
        
        if (review.getCustomer() != null) {
            dto.setCustomerId(review.getCustomer().getId());
            dto.setCustomer(review.getCustomer().getDisplayName() != null ? 
                    review.getCustomer().getDisplayName() : review.getCustomer().getUsername());
        }

        if (review.getArtist() != null) {
            dto.setArtistId(review.getArtist().getId());
        }
        
        if (review.getBooking() != null && review.getBooking().getService() != null) {
            dto.setService(review.getBooking().getService().getName());
        } else {
            dto.setService("Unknown Service");
        }
        
        dto.setRating(review.getBookingRating());
        dto.setArtistRating(review.getArtistRating());
        dto.setComment(review.getComment());
        dto.setImages(review.getImages());
        dto.setTags(review.getTags());
        
        if (review.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dto.setDate(review.getCreatedAt().format(formatter));
        } else {
            dto.setDate("N/A");
        }
        
        dto.setStatus(review.getStatus() != null ? review.getStatus() : ReviewStatus.PENDING);
        return dto;
    }

    @Transactional
    public ReviewDto createReview(CreateReviewRequest request, UUID customerId) {
        validateCreateRequest(request);
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + request.bookingId()));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access Denied: You do not own this booking");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot review a booking that is not COMPLETED");
        }
        if (reviewRepository.existsByBookingId(request.bookingId())) {
            throw new RuntimeException("Booking has already been reviewed");
        }

        BookingReview review = new BookingReview();
        review.setBooking(booking);
        review.setArtist(booking.getArtist());
        review.setCustomer(booking.getCustomer());
        review.setBookingRating(request.bookingRating());
        review.setArtistRating(request.artistRating());
        review.setComment(request.comment());
        review.setImages(request.images());
        review.setTags(request.tags());
        review.setStatus(ReviewStatus.APPROVED);

        BookingReview saved = reviewRepository.save(review);
        refreshArtistRating(saved.getArtist());
        return mapToDto(saved);
    }

    public List<ReviewDto> getReviewsByService(UUID serviceId) {
        return reviewRepository.findByBookingServiceIdAndStatusOrderByCreatedAtDesc(serviceId, ReviewStatus.APPROVED).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByArtist(UUID artistId) {
        List<BookingReview> reviews = reviewRepository.findByArtistOwnerUserIdOrderByCreatedAtDesc(artistId);
        if (reviews.isEmpty()) {
            reviews = reviewRepository.findByArtistIdAndStatusOrderByCreatedAtDesc(artistId, ReviewStatus.APPROVED);
        } else {
            reviews = reviews.stream()
                    .filter(r -> ReviewStatus.APPROVED == r.getStatus())
                    .collect(Collectors.toList());
        }
        return reviews.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByOwner(UUID ownerId) {
        return reviewRepository
                .findByArtistOwnerUserIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void validateCreateRequest(CreateReviewRequest request) {
        if (request == null || request.bookingId() == null) {
            throw new IllegalArgumentException("Booking id is required");
        }
        validateRating("Booking rating", request.bookingRating());
        validateRating("Artist rating", request.artistRating());
    }

    private void validateRating(String field, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(field + " must be between 1 and 5");
        }
    }

    private void refreshArtistRating(Artist artist) {
        if (artist == null) {
            return;
        }
        long approvedCount = reviewRepository.countByArtistIdAndStatus(artist.getId(), ReviewStatus.APPROVED);
        Double averageRating = reviewRepository.calculateAverageArtistRating(artist.getId(), ReviewStatus.APPROVED);
        artist.setReviewCount(Math.toIntExact(approvedCount));
        artist.setAverageRating(averageRating != null ? averageRating : 0.0);
        artistRepository.save(artist);
    }
}
