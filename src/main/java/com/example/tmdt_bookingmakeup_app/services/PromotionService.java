package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.CreatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.UpdatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.ValidatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.promotion.PromotionDto;
import com.example.tmdt_bookingmakeup_app.dto.response.promotion.PromotionValidationResponse;
import com.example.tmdt_bookingmakeup_app.models.promotion.Promotion;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.PromotionRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository, ServiceOwnerRepository serviceOwnerRepository) {
        this.promotionRepository = promotionRepository;
        this.serviceOwnerRepository = serviceOwnerRepository;
    }

    @Transactional
    public PromotionDto createPromotion(CreatePromotionRequest request, UUID requesterId, UserRole requesterRole) {
        // 1. Determine who is the owner of this promotion
        UUID ownerId = requesterId;
        if (requesterRole == UserRole.ADMIN && request.ownerId() != null) {
            ownerId = request.ownerId();
        }

        final UUID targetOwnerId = ownerId;

        // 2. Find ServiceOwner entity
        ServiceOwner owner = serviceOwnerRepository.findById(targetOwnerId)
                .orElseThrow(() -> new RuntimeException("ServiceOwner not found with id: " + targetOwnerId));

        // 3. Check if promotion code already exists
        Optional<Promotion> existingPromo = promotionRepository.findByCode(request.code());
        if (existingPromo.isPresent()) {
            throw new RuntimeException("Promotion code already exists: " + request.code());
        }

        // 4. Create new Promotion
        Promotion promotion = new Promotion();
        promotion.setOwner(owner);
        promotion.setCode(request.code().toUpperCase().trim());
        promotion.setDiscountValue(request.discountValue());
        promotion.setMinOrderValue(request.minOrderValue() != null ? request.minOrderValue() : 0.0);
        promotion.setPointCharge(request.pointCharge() != null ? request.pointCharge() : 0);
        promotion.setExpiryDate(request.expiryDate());

        Promotion saved = promotionRepository.save(promotion);
        return mapToDto(saved);
    }

    @Transactional
    public PromotionDto updatePromotion(UUID id, UpdatePromotionRequest request, UUID requesterId, UserRole requesterRole) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        // Authorization check: Admin can update anything. Service owner can only update their own.
        if (requesterRole != UserRole.ADMIN && !promotion.getOwner().getUserId().equals(requesterId)) {
            throw new RuntimeException("Access Denied: You do not own this promotion");
        }

        // Authorization check: Admin can update anything. Service owner can only update their own.
        if (request.discountValue() != null) {
            promotion.setDiscountValue(request.discountValue());
        }
        if (request.minOrderValue() != null) {
            promotion.setMinOrderValue(request.minOrderValue());
        }
        if (request.pointCharge() != null) {
            promotion.setPointCharge(request.pointCharge());
        }
        if (request.expiryDate() != null) {
            promotion.setExpiryDate(request.expiryDate());
        }

        Promotion updated = promotionRepository.save(promotion);
        return mapToDto(updated);
    }

    @Transactional
    public void deletePromotion(UUID id, UUID requesterId, UserRole requesterRole) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        // Authorization check
        if (requesterRole != UserRole.ADMIN && !promotion.getOwner().getUserId().equals(requesterId)) {
            throw new RuntimeException("Access Denied: You do not own this promotion");
        }

        // Authorization check
        promotionRepository.delete(promotion);
    }

    public List<PromotionDto> getPromotions(UUID requesterId, UserRole requesterRole, UUID filterOwnerId) {
        if (requesterRole == UserRole.ADMIN) {
            // Admin lists all
            return promotionRepository.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        // Check if requester is a ServiceOwner
        boolean isServiceOwner = requesterId != null && serviceOwnerRepository.existsById(requesterId);
        if (isServiceOwner) {
            // Service Owner lists their own
            return promotionRepository.findByOwnerUserId(requesterId).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        // Client/Normal user: list active (non-expired) promotions
        if (filterOwnerId != null) {
            return promotionRepository.findAllByOwnerUserIdAndExpiryDateAfter(filterOwnerId, LocalDateTime.now()).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } else {
            return promotionRepository.findAllByExpiryDateAfter(LocalDateTime.now()).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }
    }

    public PromotionDto getPromotionById(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return mapToDto(promotion);
    }

    public PromotionValidationResponse validatePromotion(ValidatePromotionRequest request) {
        Optional<Promotion> promoOpt = promotionRepository.findByCode(request.code().toUpperCase().trim());
        if (promoOpt.isEmpty()) {
            return new PromotionValidationResponse(false, 0.0, request.bookingAmount(), "Mã khuyến mãi không tồn tại", 0);
        }

        Promotion promo = promoOpt.get();

        // 1. Expiry date check
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new PromotionValidationResponse(false, 0.0, request.bookingAmount(), "Mã khuyến mãi đã hết hạn", 0);
        }

        // 2. Shop/owner check
        if (request.ownerId() != null && promo.getOwner() != null && !promo.getOwner().getUserId().equals(request.ownerId())) {
            return new PromotionValidationResponse(false, 0.0, request.bookingAmount(), "Mã khuyến mãi không áp dụng cho cửa hàng này", 0);
        }

        // 3. Minimum booking amount check
        double minVal = promo.getMinOrderValue() != null ? promo.getMinOrderValue() : 0.0;
        if (request.bookingAmount() < minVal) {
            return new PromotionValidationResponse(false, 0.0, request.bookingAmount(), "Giá trị đơn đặt lịch chưa đạt tối thiểu: " + String.format("%,.0f VNĐ", minVal), 0);
        }

        // 4. Calculate final values
        double discount = promo.getDiscountValue() != null ? promo.getDiscountValue() : 0.0;
        double finalAmount = Math.max(0.0, request.bookingAmount() - discount);

        int pointsNeeded = promo.getPointCharge() != null ? promo.getPointCharge() : 0;

        return new PromotionValidationResponse(true, discount, finalAmount, null, pointsNeeded);
    }

    private PromotionDto mapToDto(Promotion promotion) {
        PromotionDto dto = new PromotionDto();
        dto.setId(promotion.getId());
        dto.setOwnerId(promotion.getOwner() != null ? promotion.getOwner().getUserId() : null);
        dto.setCode(promotion.getCode());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setMinOrderValue(promotion.getMinOrderValue());
        dto.setPointCharge(promotion.getPointCharge());
        dto.setExpiryDate(promotion.getExpiryDate());
        return dto;
    }
}
