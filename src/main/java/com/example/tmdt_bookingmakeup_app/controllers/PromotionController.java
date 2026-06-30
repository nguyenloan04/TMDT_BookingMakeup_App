package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.CreatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.UpdatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.ValidatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.promotion.PromotionDto;
import com.example.tmdt_bookingmakeup_app.dto.response.promotion.PromotionValidationResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto;
import com.example.tmdt_bookingmakeup_app.services.PromotionService;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final UserService userService;

    @Autowired
    public PromotionController(PromotionService promotionService, UserService userService) {
        this.promotionService = promotionService;
        this.userService = userService;
    }

    @GetMapping("/platform")
    public ResponseEntity<List<PromotionDto>> getPlatformPromotions(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        try {
            if (rawUserId == null) {
                List<PromotionDto> activePromos = promotionService.getPlatformPromotions(null, UserRole.USER);
                return ResponseEntity.ok(activePromos);
            } else {
                UUID requesterId = UUID.fromString(rawUserId);
                UserDto requester = userService.getUserProfile(requesterId);
                List<PromotionDto> promos = promotionService.getPlatformPromotions(requesterId, requester.getRole());
                return ResponseEntity.ok(promos);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
    }

    // 2. Lấy mã giảm giá của một Studio cụ thể (Sẽ xử lý chi tiết sau)
    @GetMapping("/studio/{ownerId}")
    public ResponseEntity<?> getStudioPromotions(@PathVariable UUID ownerId, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        try {
            if (rawUserId == null) {
                List<PromotionDto> activePromos = promotionService.getStudioPromotions(null, UserRole.USER, ownerId);
                return ResponseEntity.ok(activePromos);
            } else {
                UUID requesterId = UUID.fromString(rawUserId);
                UserDto requester = userService.getUserProfile(requesterId);
                List<PromotionDto> promos = promotionService.getStudioPromotions(requesterId, requester.getRole(), ownerId);
                return ResponseEntity.ok(promos);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 1. Get List of Promotions (Smart filter by role and owner)
    @GetMapping
    public ResponseEntity<?> getPromotions(
            @RequestParam(required = false) UUID ownerId,
            HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            // Anonymous / non-authenticated users get active promotions filtered by ownerId
            try {
                List<PromotionDto> activePromos = promotionService.getPromotions(null, UserRole.USER, ownerId);
                return ResponseEntity.ok(activePromos);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            List<PromotionDto> promos = promotionService.getPromotions(requesterId, requester.getRole(), ownerId);
            return ResponseEntity.ok(promos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 2. Get Single Promotion Detail
    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable UUID id) {
        try {
            PromotionDto dto = promotionService.getPromotionById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Promotion not found with id: " + id);
        }
    }

    // 3. Create Promotion
    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody CreatePromotionRequest createRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            PromotionDto created = promotionService.createPromotion(createRequest, requesterId, requester.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 4. Update Promotion
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(
            @PathVariable UUID id,
            @RequestBody UpdatePromotionRequest updateRequest,
            HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            PromotionDto updated = promotionService.updatePromotion(id, updateRequest, requesterId, requester.getRole());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 5. Delete Promotion
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID id, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            promotionService.deletePromotion(id, requesterId, requester.getRole());
            return ResponseEntity.ok("Promotion deleted successfully with id: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 6. Validate Promotion Code
    @PostMapping("/validate")
    public ResponseEntity<?> validatePromotion(@RequestBody ValidatePromotionRequest validateRequest) {
        try {
            PromotionValidationResponse response = promotionService.validatePromotion(validateRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
