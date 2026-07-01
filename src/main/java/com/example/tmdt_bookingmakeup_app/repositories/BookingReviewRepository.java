package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.booking.BookingReview;
import com.example.tmdt_bookingmakeup_app.common.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingReviewRepository extends JpaRepository<BookingReview, UUID> {
    boolean existsByBookingId(UUID bookingId);
    List<BookingReview> findAllByOrderByCreatedAtDesc();
    List<BookingReview> findByBookingServiceIdAndStatusOrderByCreatedAtDesc(UUID serviceId, ReviewStatus status);
    List<BookingReview> findByArtistIdAndStatusOrderByCreatedAtDesc(UUID artistId, ReviewStatus status);
    List<BookingReview> findByArtistOwnerUserId(UUID ownerId);
    List<BookingReview> findByArtistOwnerUserIdOrderByCreatedAtDesc(UUID ownerId);
    long countByArtistIdAndStatus(UUID artistId, ReviewStatus status);

    @Query("SELECT COALESCE(AVG(r.artistRating), 0.0) FROM BookingReview r " +
            "WHERE r.artist.id = :artistId AND r.status = :status")
    Double calculateAverageArtistRating(UUID artistId, ReviewStatus status);

    @Query("SELECT r FROM BookingReview r WHERE r.booking.service.owner.userId = :ownerId")
    List<BookingReview> findAllByServiceOwnerId(@Param("ownerId") UUID ownerId);;
}
