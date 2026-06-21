package com.example.tmdt_bookingmakeup_app.models.booking;

import com.example.tmdt_bookingmakeup_app.common.enums.CommentTag;
import com.example.tmdt_bookingmakeup_app.common.enums.ReviewStatus;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.util.UUID;

@Table(
        name = "booking_reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking_reviews_booking", columnNames = "booking_id"),
        indexes = {
                @Index(name = "idx_booking_reviews_artist_status", columnList = "artist_id,status"),
                @Index(name = "idx_booking_reviews_customer", columnList = "customer_id"),
                @Index(name = "idx_booking_reviews_created_at", columnList = "created_at")
        }
)
@Check(constraints = "booking_rating BETWEEN 1 AND 5 AND artist_rating BETWEEN 1 AND 5")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "booking_rating", nullable = false)
    private Integer bookingRating;

    @Column(name = "artist_rating", nullable = false)
    private Integer artistRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String images; //FIXME: n images

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_tags", columnDefinition = "TEXT")
    private CommentTag tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
