package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.request.artist.ArtistRequestDTO;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.interaction.Follow;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public Artist createArtist(UUID soId, ArtistRequestDTO dto) {
        ServiceOwner owner = serviceOwnerRepository.findById(soId)
                .orElseThrow(() -> new RuntimeException("Studio/SO not found"));

        Artist artist = new Artist();
        artist.setOwner(owner);
        artist.setArtistName(dto.getArtistName());
        artist.setSpecialization(dto.getSpecialization());
        artist.setPortfolioImages(dto.getPortfolioImages());
        artist.setAverageRating(0.0);
        artist.setReviewCount(0);
        artist.setFollowCount(0);

        return artistRepository.save(artist);
    }

    public void deleteArtist(UUID artistId) {
        List<Booking> activeBookings = bookingRepository.findActiveBookingsByArtist(artistId);
        if (!activeBookings.isEmpty()) {
            throw new RuntimeException("Cannot delete!");
        }
        artistRepository.deleteById(artistId);
    }

    public String toggleFollowArtist(UUID customerId, UUID artistId) {
        Optional<Follow> existingFollow = followRepository.findByCustomerIdAndArtistId(customerId, artistId);

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            artistRepository.decrementFollowCount(artistId);
            return "unfollowed artist";
        } else {
            User customer = userRepository.findById(customerId).orElseThrow();
            Artist artist = artistRepository.findById(artistId).orElseThrow();

            Follow newFollow = new Follow();
            newFollow.setCustomer(customer);
            newFollow.setArtist(artist);
            followRepository.save(newFollow);

            artistRepository.incrementFollowCount(artistId);
            return "followed artist";
        }
    }
}