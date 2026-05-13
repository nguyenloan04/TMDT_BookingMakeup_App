package com.example.tmdt_bookingmakeup_app.models.booking;

import com.example.tmdt_bookingmakeup_app.common.enums.CommentTag;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "booking_reviews")
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

    @Column(nullable = false)
    private Integer booking_rating;

    @Column(nullable = false)
    private Integer artist_rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String images; //FIXME: n images

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_tags", columnDefinition = "TEXT")
    private CommentTag tags;

}
