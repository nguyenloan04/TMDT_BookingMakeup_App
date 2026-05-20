package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    public List<Booking> getArtistSchedule(UUID artistId, LocalDate date) {
        return bookingRepository.findByArtistIdAndBookingDate(artistId, date);
    }

    public Booking rescheduleBooking(UUID bookingId, LocalDate newDate, LocalTime newStart, LocalTime newEnd) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                booking.getArtist().getId(), newDate, newStart, newEnd);

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Cannot create booking in this time!");
        }

        booking.setBookingDate(newDate);
        booking.setStartTime(newStart);
        booking.setEndTime(newEnd);
        return bookingRepository.save(booking);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUniqueCustomers", bookingRepository.countUniqueCustomers());
        stats.put("topServices", bookingRepository.findTopServices(PageRequest.of(0, 5)));
        stats.put("topCustomers", bookingRepository.findTopCustomers(PageRequest.of(0, 10)));

        return stats;
    }
}
