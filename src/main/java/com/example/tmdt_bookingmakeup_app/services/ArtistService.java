package com.example.tmdt_bookingmakeup_app.services;


import com.example.tmdt_bookingmakeup_app.dto.request.artist.ArtistRequestDTO;
import com.example.tmdt_bookingmakeup_app.dto.response.artist.ArtistResponseDTO;
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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public ArtistResponseDTO createArtist(UUID soId, ArtistRequestDTO dto) {
        ServiceOwner owner = serviceOwnerRepository.findById(soId)
                .orElseThrow(() -> new RuntimeException("Studio/SO not found")); //

        Artist artist = new Artist();
        artist.setOwner(owner); //
        artist.setArtistName(dto.getArtistName()); //
        artist.setSpecialization(dto.getSpecialization()); //
        artist.setPortfolioImages(dto.getPortfolioImages()); //
        artist.setAverageRating(0.0); //
        artist.setReviewCount(0); //
        artist.setFollowCount(0); //

        Artist savedArtist = artistRepository.save(artist); //
        return ArtistResponseDTO.fromEntity(savedArtist);
    }

    public List<ArtistResponseDTO> getArtistsByOwnerId(UUID ownerId) {
        List<Artist> artists = artistRepository.findByOwnerUserId(ownerId);
        return artists.stream()
                .map(ArtistResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ArtistResponseDTO getArtistById(UUID artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + artistId));
        return ArtistResponseDTO.fromEntity(artist);
    }

    public void deleteArtist(UUID artistId) {
        List<Booking> activeBookings = bookingRepository.findActiveBookingsByArtist(artistId); //
        if (!activeBookings.isEmpty()) { //
            throw new RuntimeException("Cannot delete! Artist has active bookings."); //
        }
        artistRepository.deleteById(artistId); //
    }

    @Transactional
    public void toggleFollow(UUID artistId, String customerIdStr) {
        UUID customerId = UUID.fromString(customerIdStr);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Artist"));

        Optional<Follow> existingFollow = followRepository.findByCustomerIdAndArtistId(customerId, artistId);

        if (existingFollow.isPresent()) {
            // NẾU ĐÃ FOLLOW -> HỦY FOLLOW
            followRepository.delete(existingFollow.get());
            artist.setFollowCount(Math.max(0, artist.getFollowCount() - 1)); // Giảm count
        } else {
            // NẾU CHƯA FOLLOW -> THÊM FOLLOW
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

            Follow newFollow = new Follow();
            newFollow.setCustomer(customer);
            newFollow.setArtist(artist);
            followRepository.save(newFollow);

            artist.setFollowCount(artist.getFollowCount() + 1); // Tăng count
        }

        artistRepository.save(artist); // Lưu lại số lượng follow mới
    }

    public boolean checkFollowStatus(UUID artistId, String customerIdStr) {
        if (customerIdStr == null) return false;
        UUID customerId = UUID.fromString(customerIdStr);
        return followRepository.existsByCustomerIdAndArtistId(customerId, artistId);
    }
}