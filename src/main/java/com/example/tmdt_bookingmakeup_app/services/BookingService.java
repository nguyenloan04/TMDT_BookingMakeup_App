package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.booking.CreateBookingRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.booking.UpdateBookingStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.promotion.ValidatePromotionRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.booking.BookingDto;
import com.example.tmdt_bookingmakeup_app.dto.response.home.FeaturedArtistDto;
import com.example.tmdt_bookingmakeup_app.dto.response.promotion.PromotionValidationResponse;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.promotion.Promotion;
import com.example.tmdt_bookingmakeup_app.models.services.Service;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ArtistRepository artistRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;
    private final NotificationService notificationService;

    @Transactional
    public BookingDto createBooking(CreateBookingRequest request, UUID customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + request.serviceId()));

        Artist artist = artistRepository.findByOwnerUserId(request.ownerId()).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Chủ tiệm này hiện chưa có thợ nào để nhận lịch!"));

        // 2. Calculate end time based on service duration (default to 60 minutes if null)
        int durationMinutes = service.getDuration() != null ? service.getDuration() : 60;
        LocalTime endTime = request.startTime().plusMinutes(durationMinutes);

        // 3. Integrate Promotion system
        double basePrice = service.getPrice();
        double discountAmount = 0.0;

        Promotion appliedPromo = null;
        int pointsToDeduct = 0;
        if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
            UUID ownerId = service.getOwner() != null ? service.getOwner().getUserId() : null;
            ValidatePromotionRequest valRequest = new ValidatePromotionRequest(request.promoCode(), basePrice, ownerId);

            PromotionValidationResponse valResponse = promotionService.validatePromotion(valRequest);
            if (!valResponse.isValid()) {
                throw new RuntimeException("Không thể áp dụng mã: " + valResponse.getErrorMessage());
            }

            appliedPromo = promotionRepository.findByCode(request.promoCode())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã khuyến mãi"));

            // Check xem mã có tốn điểm không
            if (appliedPromo.getPointCharge() != null && appliedPromo.getPointCharge() > 0) {
                int currentPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;
                if (currentPoints < appliedPromo.getPointCharge()) {
                    throw new RuntimeException("Bạn không đủ điểm để đổi mã khuyến mãi này!");
                }
                pointsToDeduct = appliedPromo.getPointCharge();
            }

            discountAmount = valResponse.getDiscountAmount();
        }

        double totalAmount = Math.max(0.0, basePrice - discountAmount);
        double depositAmount = totalAmount * 0.55; // tiền cọc, sẽ sửa lại sau gọi tiền cọc từ trang chi tiet dịch vụ, ko lấy cố định cọc
        double platformFee = totalAmount * 0.15;  // 15% phí nền tảng (lấy 15 của tiền dịch vụ), sẽ lấy của artist

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
        booking.setStatus(BookingStatus.valueOf(BookingStatus.PENDING.name()));
        booking.setPromotion(appliedPromo);
        booking.setUsedPoints(pointsToDeduct);

        if (pointsToDeduct > 0) {
            int currentPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;
            customer.setTotalPoints(currentPoints - pointsToDeduct);
            userRepository.saveAndFlush(customer);
        }

        Booking saved = bookingRepository.save(booking);
        notificationService.notifyBookingStatusChange(saved, BookingStatus.PENDING, customerId);
        return mapToDto(saved);
    }


    public List<Booking> getArtistSchedule(UUID artistId, LocalDate date) {
        return bookingRepository.findByArtistIdAndBookingDate(artistId, date);
    }

    public Booking rescheduleBooking(UUID bookingId, LocalDate newDate, LocalTime newStart, LocalTime newEnd) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

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


    public List<BookingDto> getBookings(UUID requesterId, UserRole role) {
        List<Booking> bookings;

        if (role == UserRole.ADMIN) {
            bookings = bookingRepository.findAll();
        } else {
            boolean isSO = serviceOwnerRepository.existsById(requesterId);
            if (isSO) {
                bookings = bookingRepository.findByServiceOwnerUserId(requesterId);
            } else {
                bookings = bookingRepository.findByCustomerId(requesterId);
            }
        }

        return bookings.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUniqueCustomers", bookingRepository.countUniqueCustomers());
        stats.put("topServices", bookingRepository.findTopServices(PageRequest.of(0, 5)));
        stats.put("topCustomers", bookingRepository.findTopCustomers(PageRequest.of(0, 10)));
        return stats;
    }

    public BookingDto getBookingById(UUID id, UUID requesterId, UserRole requesterRole) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        UUID ownerId = booking.getService().getOwner() != null ? booking.getService().getOwner().getUserId() : null;
        if (requesterRole != UserRole.ADMIN
                && !booking.getCustomer().getId().equals(requesterId)
                && !requesterId.equals(ownerId)) {
            throw new RuntimeException("Access Denied: You are not authorized to view this booking");
        }

        return mapToDto(booking);
    }
    public List<BookingDto> getBookingsByArtistId(UUID artistId, String requesterId) {
        List<Booking> bookings = bookingRepository.findByArtistId(artistId);

        return bookings.stream().map(booking -> {
            BookingDto dto = mapToDto(booking);

            // Kiểm tra quyền sở hữu (Owner của Service)
            UUID ownerId = (booking.getService() != null && booking.getService().getOwner() != null)
                    ? booking.getService().getOwner().getUserId() : null;

            boolean isOwner = (requesterId != null && ownerId != null && ownerId.toString().equals(requesterId));

            // Nếu KHÔNG PHẢI chủ tiệm và KHÔNG PHẢI chính khách đặt lịch -> Che dữ liệu
            boolean isCustomer = (requesterId != null && booking.getCustomer() != null && booking.getCustomer().getId().toString().equals(requesterId));

            if (!isOwner && !isCustomer) {
                dto.setCustomerId(null);
                dto.setCustomerDisplayName("Khách hàng (Bảo mật)");
                dto.setTotalAmount(null);
                dto.setDepositAmount(null);
                dto.setPlatformFee(null);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public BookingDto updateBookingStatus(UUID bookingId, UpdateBookingStatusRequest request, UUID requesterId, UserRole role) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt lịch"));

        BookingStatus requestedStatus;
        try {
            requestedStatus = BookingStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + request.status());
        }

        // 1. Nếu người yêu cầu là CUSTOMER (Khách hàng)
        if (role == UserRole.USER && booking.getCustomer().getId().equals(requesterId)) {
            // Khách CHỈ được phép gửi request CANCELLED
            if (requestedStatus != BookingStatus.CANCELLED) {
                throw new RuntimeException("Khách hàng chỉ được phép Hủy lịch hẹn");
            }
            // Khách chỉ được hủy khi lịch đang ở trạng thái PENDING hoặc CONFIRMED (chưa thanh toán cọc)
            if (booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.COMPLETED) {
                throw new RuntimeException("Không thể hủy vì đơn hàng đã được thanh toán hoặc hoàn thành");
            }
            booking.setStatus(BookingStatus.CANCELLED);
        }

        // 2. Nếu người yêu cầu là SO (Chủ tiệm sở hữu dịch vụ này)
        else if (role == UserRole.USER && booking.getService().getOwner().getUserId().equals(requesterId)) {
            BookingStatus newStatus;
            try {
                newStatus = BookingStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái không hợp lệ");
            }

            if (requestedStatus == BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.COMPLETED) {
                double totalPaid = booking.getDepositAmount();
                int earnedPoints = (int) (totalPaid / 10000);   //10.000 VND = 1 point

                if (earnedPoints > 0) {
                    User customer = booking.getCustomer();
                    int currentPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;
                    customer.setTotalPoints(currentPoints + earnedPoints);
                    userRepository.save(customer);
                }
            }

            booking.setStatus(newStatus);
        }

        // 3. Nếu là Admin
        else if (role == UserRole.ADMIN) {
            booking.setStatus(BookingStatus.valueOf(request.status()));
        }

        else {
            throw new RuntimeException("Bạn không có quyền thay đổi trạng thái đơn này");
        }

        booking = bookingRepository.save(booking);
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
        dto.setStatus(String.valueOf(booking.getStatus()));
        return dto;
    }
}
