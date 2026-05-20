package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.dto.request.artist.TopCustomerDTO;
import com.example.tmdt_bookingmakeup_app.dto.request.artist.TopServiceDTO;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByArtistIdAndBookingDate(UUID artistId, LocalDate bookingDate);

    @Query("SELECT b FROM Booking b WHERE b.artist.id = :artistId AND b.status IN ('PENDING', 'PAID_DEPOSIT', 'CONFIRMED')")
    List<Booking> findActiveBookingsByArtist(@Param("artistId") UUID artistId);

    @Query("SELECT b FROM Booking b WHERE b.artist.id = :artistId " +
            "AND b.bookingDate = :date " +
            "AND b.status IN ('CONFIRMED') " +
            "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findConflictingBookings(@Param("artistId") UUID artistId,
                                          @Param("date") LocalDate date,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);


    @Query("SELECT COUNT(DISTINCT b.customer.id) FROM Booking b WHERE b.status = 'COMPLETED'")
    Long countUniqueCustomers();

    @Query("SELECT new com.example.tmdt_bookingmakeup_app.dto.request.artist.TopServiceDTO(s.id, s.name, COUNT(b.id)) " +
            "FROM Booking b JOIN b.service s " +
            "WHERE b.status = 'COMPLETED' " +
            "GROUP BY s.id, s.name ORDER BY COUNT(b.id) DESC")
    List<TopServiceDTO> findTopServices(Pageable pageable);

    @Query("SELECT new com.example.tmdt_bookingmakeup_app.dto.request.artist.TopCustomerDTO(u.id, u.displayName, u.email, COUNT(b.id), SUM(b.totalAmount)) " +
            "FROM Booking b JOIN b.customer u " +
            "WHERE b.status = 'COMPLETED' " +
            "GROUP BY u.id, u.displayName, u.email ORDER BY COUNT(b.id) DESC")
    List<TopCustomerDTO> findTopCustomers(Pageable pageable);
}
