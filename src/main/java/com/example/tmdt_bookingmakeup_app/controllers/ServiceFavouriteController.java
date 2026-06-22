package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.response.service.ServiceDto;
import com.example.tmdt_bookingmakeup_app.services.ServiceFavouriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/favourites")
public class ServiceFavouriteController {

    private final ServiceFavouriteService favouriteService;

    @Autowired
    public ServiceFavouriteController(ServiceFavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @PostMapping("/{serviceId}")
    public ResponseEntity<?> addFavourite(@PathVariable UUID serviceId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID customerId = UUID.fromString(rawUserId);
            favouriteService.addFavourite(customerId, serviceId);
            return ResponseEntity.ok("Added to favourites successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> removeFavourite(@PathVariable UUID serviceId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID customerId = UUID.fromString(rawUserId);
            favouriteService.removeFavourite(customerId, serviceId);
            return ResponseEntity.ok("Removed from favourites successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getFavourites(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID customerId = UUID.fromString(rawUserId);
            List<ServiceDto> favourites = favouriteService.getFavourites(customerId);
            return ResponseEntity.ok(favourites);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{serviceId}/status")
    public ResponseEntity<?> isFavourite(@PathVariable UUID serviceId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID customerId = UUID.fromString(rawUserId);
            boolean status = favouriteService.isFavourite(customerId, serviceId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
