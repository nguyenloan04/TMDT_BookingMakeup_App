package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.response.statistics.BookingDetailDto;
import com.example.tmdt_bookingmakeup_app.dto.response.statistics.BookingStatisticsResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.statistics.RevenueStatisticsResponse;
import com.example.tmdt_bookingmakeup_app.dto.response.statistics.ServiceOwnerVerificationDto;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final BookingRepository bookingRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;

    @Autowired
    public StatisticsService(BookingRepository bookingRepository, ServiceOwnerRepository serviceOwnerRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceOwnerRepository = serviceOwnerRepository;
    }

    public RevenueStatisticsResponse getRevenueStatistics(UUID requesterId, UserRole role) {
        List<Booking> bookings = getFilteredBookings(requesterId, role);

        // Calculate totals for COMPLETED, PAID, and CONFIRMED bookings
        double totalRevenue = 0.0;
        double platformFee = 0.0;
        double totalDeposit = 0.0;

        for (Booking booking : bookings) {
            BookingStatus status = booking.getStatus();
            if (status == BookingStatus.COMPLETED || status == BookingStatus.PAID || status == BookingStatus.CONFIRMED) {
                totalRevenue += booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
                platformFee += booking.getPlatformFee() != null ? booking.getPlatformFee() : 0.0;
                totalDeposit += booking.getDepositAmount() != null ? booking.getDepositAmount() : 0.0;
            }
        }

        double studioRevenue = totalRevenue - platformFee;

        // Map all bookings for the cash-flow detail table (ordered by booking date desc)
        List<BookingDetailDto> bookingDetails = bookings.stream()
                .sorted((b1, b2) -> {
                    if (b1.getBookingDate() == null || b2.getBookingDate() == null) return 0;
                    int dateCompare = b2.getBookingDate().compareTo(b1.getBookingDate());
                    if (dateCompare != 0) return dateCompare;
                    if (b1.getStartTime() == null || b2.getStartTime() == null) return 0;
                    return b2.getStartTime().compareTo(b1.getStartTime());
                })
                .map(this::mapToDetailDto)
                .collect(Collectors.toList());

        return RevenueStatisticsResponse.builder()
                .totalRevenue(totalRevenue)
                .platformFee(platformFee)
                .studioRevenue(studioRevenue)
                .totalDeposit(totalDeposit)
                .bookings(bookingDetails)
                .build();
    }

    public BookingStatisticsResponse getBookingStatistics(UUID requesterId, UserRole role) {
        List<Booking> bookings = getFilteredBookings(requesterId, role);

        long totalBookings = bookings.size();
        long pendingCount = 0;
        long confirmedCount = 0;
        long paidCount = 0;
        long completedCount = 0;
        long cancelledCount = 0;

        for (Booking booking : bookings) {
            BookingStatus status = booking.getStatus();
            if (status != null) {
                switch (status) {
                    case PENDING -> pendingCount++;
                    case CONFIRMED -> confirmedCount++;
                    case PAID -> paidCount++;
                    case COMPLETED -> completedCount++;
                    case CANCELLED, REJECTED -> cancelledCount++;
                }
            }
        }

        // Calculate unique customers count
        long customerCount = bookings.stream()
                .filter(b -> b.getCustomer() != null)
                .map(b -> b.getCustomer().getId())
                .distinct()
                .count();

        // Calculate percentages
        double pendingPercentage = 0.0;
        double confirmedPercentage = 0.0;
        double paidPercentage = 0.0;
        double completedPercentage = 0.0;
        double cancelledPercentage = 0.0;

        double completionRate = 0.0;
        double cancellationRate = 0.0;

        if (totalBookings > 0) {
            pendingPercentage = roundToPercentage((pendingCount * 100.0) / totalBookings);
            confirmedPercentage = roundToPercentage((confirmedCount * 100.0) / totalBookings);
            paidPercentage = roundToPercentage((paidCount * 100.0) / totalBookings);
            completedPercentage = roundToPercentage((completedCount * 100.0) / totalBookings);
            cancelledPercentage = roundToPercentage((cancelledCount * 100.0) / totalBookings);

            completionRate = roundToPercentage((completedCount * 100.0) / totalBookings);
            cancellationRate = roundToPercentage((cancelledCount * 100.0) / totalBookings);
        }

        return BookingStatisticsResponse.builder()
                .totalBookings(totalBookings)
                .pendingCount(pendingCount)
                .confirmedCount(confirmedCount)
                .paidCount(paidCount)
                .completedCount(completedCount)
                .cancelledCount(cancelledCount)
                .customerCount(customerCount)
                .pendingPercentage(pendingPercentage)
                .confirmedPercentage(confirmedPercentage)
                .paidPercentage(paidPercentage)
                .completedPercentage(completedPercentage)
                .cancelledPercentage(cancelledPercentage)
                .completionRate(completionRate)
                .cancellationRate(cancellationRate)
                .build();
    }

    private List<Booking> getFilteredBookings(UUID requesterId, UserRole role) {
        if (role == UserRole.ADMIN) {
            return bookingRepository.findAll();
        } else {
            boolean isSO = serviceOwnerRepository.existsById(requesterId);
            if (isSO) {
                return bookingRepository.findByServiceOwnerUserId(requesterId);
            } else {
                throw new RuntimeException("Access Denied: Only Admins and Service Owners are allowed to view statistics.");
            }
        }
    }

    private BookingDetailDto mapToDetailDto(Booking booking) {
        double total = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
        double fee = booking.getPlatformFee() != null ? booking.getPlatformFee() : 0.0;
        double studioReceives = total - fee;

        return BookingDetailDto.builder()
                .id(booking.getId())
                .bookingDate(booking.getBookingDate())
                .customerName(booking.getCustomer() != null ? booking.getCustomer().getDisplayName() : "Khách ẩn danh")
                .serviceName(booking.getService() != null ? booking.getService().getName() : "Dịch vụ đã xóa")
                .totalAmount(total)
                .depositAmount(booking.getDepositAmount() != null ? booking.getDepositAmount() : 0.0)
                .platformFee(fee)
                .studioReceives(studioReceives)
                .status(booking.getStatus() != null ? booking.getStatus().name() : "")
                .statusLabel(translateStatus(booking.getStatus()))
                .build();
    }

    private String translateStatus(BookingStatus status) {
        if (status == null) return "Chờ duyệt";
        return switch (status) {
            case PENDING -> "Chờ duyệt";
            case CONFIRMED -> "Đã xác nhận";
            case PAID -> "Đã thanh toán / đặt cọc";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED, REJECTED -> "Đã hủy";
        };
    }

    private double roundToPercentage(double value) {
        return Math.round(value * 10.0) / 10.0; // round to 1 decimal place
    }

    public List<ServiceOwnerVerificationDto> getPendingVerifications(UUID requesterId, UserRole role) {
        checkAdminAccess(role);
        List<ServiceOwner> all = serviceOwnerRepository.findAll();
        return all.stream()
                .map(this::mapToVerificationDto)
                .collect(Collectors.toList());
    }

    public ServiceOwnerVerificationDto getVerificationDetail(UUID userId, UUID requesterId, UserRole role) {
        checkAdminAccess(role);
        ServiceOwner so = serviceOwnerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ đăng ký của người dùng với ID: " + userId));
        return mapToVerificationDto(so);
    }

    public ServiceOwnerVerificationDto approveVerification(UUID userId, UUID requesterId, UserRole role) {
        checkAdminAccess(role);
        ServiceOwner so = serviceOwnerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ đăng ký của người dùng với ID: " + userId));
        so.setVerificationStatus("approved");
        ServiceOwner saved = serviceOwnerRepository.save(so);
        return mapToVerificationDto(saved);
    }

    public ServiceOwnerVerificationDto rejectVerification(UUID userId, UUID requesterId, UserRole role) {
        checkAdminAccess(role);
        ServiceOwner so = serviceOwnerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ đăng ký của người dùng với ID: " + userId));
        so.setVerificationStatus("rejected");
        ServiceOwner saved = serviceOwnerRepository.save(so);
        return mapToVerificationDto(saved);
    }

    private void checkAdminAccess(UserRole role) {
        if (role != UserRole.ADMIN) {
            throw new RuntimeException("Access Denied: Only Admins can manage verification requests.");
        }
    }

    private ServiceOwnerVerificationDto mapToVerificationDto(ServiceOwner so) {
        if (so == null) return null;
        return ServiceOwnerVerificationDto.builder()
                .userId(so.getUserId())
                .displayName(so.getUser() != null ? so.getUser().getDisplayName() : "Khách ẩn danh")
                .email(so.getUser() != null ? so.getUser().getEmail() : "")
                .phone(so.getUser() != null ? so.getUser().getPhone() : "")
                .bio(so.getBio())
                .experienceYears(so.getExperienceYears())
                .showcaseType(so.getShowcaseType())
                .identityFront(so.getIdentityFront())
                .identityBack(so.getIdentityBack())
                .verificationStatus(so.getVerificationStatus())
                .build();
    }
}
