package com.example.tmdt_bookingmakeup_app.models.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "artists")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private ServiceOwner owner;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    private String specialization;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "follow_count")
    private Integer followCount = 0;

    @Column(columnDefinition = "TEXT")
    private String portfolioImages; //FIXME: 1 artist may have n images
}
