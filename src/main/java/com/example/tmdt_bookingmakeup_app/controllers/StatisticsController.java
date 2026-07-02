package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.response.statistics.BookingStatisticsResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.statistics.RevenueStatisticsResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.statistics.ServiceOwnerVerificationDto;
import com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto;
import com.example.tmdt_bookingmakeup_app.services.StatisticsService;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService, UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request
    ) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            RevenueStatisticsResponse stats = statisticsService.getRevenueStatistics(requesterId, requester.getRole(), startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getBookingStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request
    ) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            BookingStatisticsResponse stats = statisticsService.getBookingStatistics(requesterId, requester.getRole(), startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/verifications")
    public ResponseEntity<?> getPendingVerifications(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            List<ServiceOwnerVerificationDto> list = statisticsService.getPendingVerifications(requesterId, requester.getRole());
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/verifications/{userId}")
    public ResponseEntity<?> getVerificationDetail(@PathVariable UUID userId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            ServiceOwnerVerificationDto detail = statisticsService.getVerificationDetail(userId, requesterId, requester.getRole());
            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/verifications/{userId}/approve")
    public ResponseEntity<?> approveVerification(@PathVariable UUID userId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            ServiceOwnerVerificationDto approved = statisticsService.approveVerification(userId, requesterId, requester.getRole());
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/verifications/{userId}/reject")
    public ResponseEntity<?> rejectVerification(@PathVariable UUID userId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            ServiceOwnerVerificationDto rejected = statisticsService.rejectVerification(userId, requesterId, requester.getRole());
            return ResponseEntity.ok(rejected);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
