package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.request.booking.CreateBookingRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.booking.UpdateBookingStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.booking.BookingDto;
import com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto;
import com.example.tmdt_bookingmakeup_app.services.BookingService;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    // 1. Place a Booking
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest createRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID customerId = UUID.fromString(rawUserId);
            BookingDto created = bookingService.createBooking(createRequest, customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 2. Get Bookings List (Smart filter based on user roles)
    @GetMapping
    public ResponseEntity<?> getBookings(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            List<BookingDto> bookings = bookingService.getBookings(requesterId, requester.getRole());
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 3. Get Booking Details by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable UUID id, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            BookingDto dto = bookingService.getBookingById(id, requesterId, requester.getRole());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 4. Update Booking Status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable UUID id,
            @RequestBody UpdateBookingStatusRequest statusRequest,
            HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            BookingDto updated = bookingService.updateBookingStatus(id, statusRequest, requesterId, requester.getRole());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<?> getBookingsByArtist(@PathVariable UUID artistId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            List<BookingDto> bookings = bookingService.getBookingsByArtistId(artistId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
