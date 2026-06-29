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
    private final com.example.tmdt_bookingmakeup_app.services.UserService userService;

    @Autowired
    public ServiceFavouriteController(ServiceFavouriteService favouriteService, com.example.tmdt_bookingmakeup_app.services.UserService userService) {
        this.favouriteService = favouriteService;
        this.userService = userService;
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

    // 5. Admin API: Get All Favourites
    @GetMapping("/admin")
    public ResponseEntity<?> getAllFavouritesAdmin(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != com.example.tmdt_bookingmakeup_app.common.enums.UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            return ResponseEntity.ok(favouriteService.getAllFavouritesForAdmin());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 6. Admin API: Delete Favorite association by ID
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteFavouriteAdmin(@PathVariable Long id, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != com.example.tmdt_bookingmakeup_app.common.enums.UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            favouriteService.deleteFavouriteById(id);
            return ResponseEntity.ok("Favorite record deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
