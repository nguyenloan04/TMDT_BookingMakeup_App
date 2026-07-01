package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.config.SePayConfig;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import com.example.tmdt_bookingmakeup_app.services.NotificationService;
import com.example.tmdt_bookingmakeup_app.services.payment.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentIPN {
    private final BookingRepository bookingRepository;
    private final SePayConfig sePayConfig;
    private final NotificationService notificationService;
    private final WalletService walletService;
    private final UserRepository userRepository;

    @PostMapping("/ipn")
    public ResponseEntity<?> handleSePayIPN(
            @RequestHeader(value = "Authorization", required = false) String secretKey,
            @RequestBody Map<String, Object> payload) {

        try {
            // 1. Lấy thẳng dữ liệu từ payload (Không qua biến "order")
            String transferContent = (String) payload.get("content");
            Number transferAmountNum = (Number) payload.get("transferAmount");

            if (transferContent == null || transferAmountNum == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Dữ liệu không hợp lệ"));
            }

            double transferAmount = transferAmountNum.doubleValue();

            // 2. Tìm mã Booking ID (UUID) bị kẹp trong nội dung chuyển khoản
            String invoiceStr = extractUUIDFromString(transferContent);

            if (invoiceStr != null) {
                UUID bookingId = UUID.fromString(invoiceStr);
                Booking booking = bookingRepository.findById(bookingId).orElse(null);

                if (booking != null && booking.getStatus() == BookingStatus.PENDING) {

                    // (Tùy chọn) Nên có 1 vòng if kiểm tra transferAmount có >= booking.getDepositAmount() không

                    booking.setStatus(BookingStatus.PAID);
                    bookingRepository.save(booking);

                    double deposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : 0.0;
                    double platformFee = booking.getPlatformFee() != null ? booking.getPlatformFee() : 0.0;
                    double ownerEarnings = deposit - platformFee;

                    if (ownerEarnings > 0) {
                        UUID ownerId = booking.getService().getOwner().getUserId();
                        walletService.addFunds(ownerId, ownerEarnings);
                    }

                    int earnedPoints = (int) (deposit / 10000);   // 10.000 VND = 1 point

                    if (earnedPoints > 0) {
                        User customer = booking.getCustomer();
                        int currentPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;
                        customer.setTotalPoints(currentPoints + earnedPoints);
                        userRepository.save(customer);
                    }

                    notificationService.notifyPaymentSuccess(bookingId);
                    System.out.println("ĐÃ XỬ LÝ THÀNH CÔNG ĐƠN HÀNG: " + bookingId);
                }
            } else {
                System.out.println("Không tìm thấy UUID trong nội dung CK: " + transferContent);
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý IPN: " + e.getMessage());
        }

        // Bắt buộc trả về 200 OK cho SePay
        return ResponseEntity.ok(Map.of("success", true));
    }

    private String extractUUIDFromString(String text) {
        Pattern p = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return null;
    }
}