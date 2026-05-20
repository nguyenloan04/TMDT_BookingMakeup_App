package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.booking.CreateBookingRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.booking.UpdateBookingStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.ValidatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.booking.BookingDto;
import com.example.tmdt_bookingmakeup_app.dto.response.promotion.PromotionValidationResponse;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ArtistRepository artistRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;
    private final PromotionService promotionService;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            ArtistRepository artistRepository,
            ServiceOwnerRepository serviceOwnerRepository,
            PromotionService promotionService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.artistRepository = artistRepository;
        this.serviceOwnerRepository = serviceOwnerRepository;
        this.promotionService = promotionService;
    }

    @Transactional
    public BookingDto createBooking(CreateBookingRequest request, UUID customerId) {
        // 1. Fetch user, service and artist
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + request.serviceId()));

        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + request.artistId()));

        // 2. Calculate end time based on service duration (default to 60 minutes if null)
        int durationMinutes = service.getDuration() != null ? service.getDuration() : 60;
        LocalTime endTime = request.startTime().plusMinutes(durationMinutes);

        // 3. Integrate Promotion system
        double basePrice = service.getPrice();
        double discountAmount = 0.0;

        if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
            UUID ownerId = service.getOwner() != null ? service.getOwner().getUserId() : null;
            ValidatePromotionRequest valRequest = new ValidatePromotionRequest(
                    request.promoCode(),
                    basePrice,
                    ownerId
            );
            PromotionValidationResponse valResponse = promotionService.validatePromotion(valRequest);
            if (valResponse.isValid()) {
                discountAmount = valResponse.getDiscountAmount();
            } else {
                // Throw exception if promo code was sent but was invalid
                throw new RuntimeException("Không thể áp dụng mã giảm giá: " + valResponse.getErrorMessage());
            }
        }

        double totalAmount = Math.max(0.0, basePrice - discountAmount);
        double depositAmount = totalAmount * 0.2; // Default to 20% deposit
        double platformFee = totalAmount * 0.05;  // Default to 5% platform fee

        // 4. Save Booking Entity
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setService(service);
        booking.setArtist(artist);
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(endTime);
        booking.setTotalAmount(totalAmount);
        booking.setDepositAmount(depositAmount);
        booking.setPlatformFee(platformFee);
        booking.setStatus("PENDING");

        Booking saved = bookingRepository.save(booking);
        return mapToDto(saved);
    }

    @Transactional
    public BookingDto updateBookingStatus(UUID bookingId, UpdateBookingStatusRequest request, UUID requesterId, UserRole requesterRole) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        String newStatus = request.status().toUpperCase().trim();
        String currentStatus = booking.getStatus().toUpperCase();

        // 1. If requester is Admin: allowed to change to any status
        if (requesterRole == UserRole.ADMIN) {
            booking.setStatus(newStatus);
            Booking saved = bookingRepository.save(booking);
            return mapToDto(saved);
        }

        // 2. If requester is the Customer: can ONLY cancel their own PENDING or CONFIRMED booking
        if (booking.getCustomer().getId().equals(requesterId)) {
            if (!newStatus.equals("CANCELLED")) {
                throw new RuntimeException("Access Denied: Customers can only transition status to CANCELLED");
            }
            if (!currentStatus.equals("PENDING") && !currentStatus.equals("CONFIRMED")) {
                throw new RuntimeException("Cannot cancel booking. Current status is: " + currentStatus);
            }
            booking.setStatus("CANCELLED");
            Booking saved = bookingRepository.save(booking);
            return mapToDto(saved);
        }

        // 3. If requester is the ServiceOwner: can CONFIRM, COMPLETE, or CANCEL/REJECT bookings for their shop
        UUID ownerId = booking.getService().getOwner() != null ? booking.getService().getOwner().getUserId() : null;
        if (requesterId.equals(ownerId)) {
            if (newStatus.equals("CONFIRMED")) {
                if (!currentStatus.equals("PENDING")) {
                    throw new RuntimeException("Cannot confirm booking from status: " + currentStatus);
                }
            } else if (newStatus.equals("COMPLETED")) {
                if (!currentStatus.equals("CONFIRMED")) {
                    throw new RuntimeException("Cannot complete booking from status: " + currentStatus);
                }
            } else if (newStatus.equals("CANCELLED")) {
                // Allowed to reject/cancel from PENDING or CONFIRMED
                if (!currentStatus.equals("PENDING") && !currentStatus.equals("CONFIRMED")) {
                    throw new RuntimeException("Cannot cancel booking from status: " + currentStatus);
                }
            } else {
                throw new RuntimeException("Invalid status transition for Service Owner: " + newStatus);
            }

            booking.setStatus(newStatus);
            Booking saved = bookingRepository.save(booking);
            return mapToDto(saved);
        }

        throw new RuntimeException("Access Denied: You are not authorized to manage this booking");
    }

    public List<BookingDto> getBookings(UUID requesterId, UserRole requesterRole) {
        if (requesterRole == UserRole.ADMIN) {
            // Admin lists all bookings in system
            return bookingRepository.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        // Check if requester is a ServiceOwner
        boolean isServiceOwner = serviceOwnerRepository.existsById(requesterId);
        if (isServiceOwner) {
            // Service Owner lists bookings placed at their shop
            return bookingRepository.findByServiceOwnerUserId(requesterId).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }

        // Otherwise: list bookings placed by this customer
        return bookingRepository.findByCustomerId(requesterId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BookingDto getBookingById(UUID id, UUID requesterId, UserRole requesterRole) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        // Authorization check: Admin, Customer who placed it, or Service Owner who owns the service can view
        UUID ownerId = booking.getService().getOwner() != null ? booking.getService().getOwner().getUserId() : null;
        if (requesterRole != UserRole.ADMIN 
                && !booking.getCustomer().getId().equals(requesterId) 
                && !requesterId.equals(ownerId)) {
            throw new RuntimeException("Access Denied: You are not authorized to view this booking");
        }

        return mapToDto(booking);
    }

    private BookingDto mapToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setCustomerId(booking.getCustomer() != null ? booking.getCustomer().getId() : null);
        dto.setCustomerDisplayName(booking.getCustomer() != null ? booking.getCustomer().getDisplayName() : null);
        dto.setServiceId(booking.getService() != null ? booking.getService().getId() : null);
        dto.setServiceName(booking.getService() != null ? booking.getService().getName() : null);
        dto.setServicePrice(booking.getService() != null ? booking.getService().getPrice() : null);
        dto.setArtistId(booking.getArtist() != null ? booking.getArtist().getId() : null);
        dto.setArtistName(booking.getArtist() != null ? booking.getArtist().getArtistName() : null);
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setDepositAmount(booking.getDepositAmount());
        dto.setPlatformFee(booking.getPlatformFee());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
