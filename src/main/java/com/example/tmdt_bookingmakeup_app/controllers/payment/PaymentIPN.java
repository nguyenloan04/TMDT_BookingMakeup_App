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
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey,
            @RequestBody Map<String, Object> payload) {

        if (secretKey == null || !secretKey.equals(sePayConfig.secretKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        String notificationType = (String) payload.get("notification_type");

        if ("ORDER_PAID".equals(notificationType)) {
            Map<String, Object> orderInfo = (Map<String, Object>) payload.get("order");
            String invoiceStr = (String) orderInfo.get("order_invoice_number");

            try {
                UUID bookingId = UUID.fromString(invoiceStr);
                Booking booking = bookingRepository.findById(bookingId).orElse(null);

                if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(BookingStatus.PAID);
                    bookingRepository.save(booking);

                    double deposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : 0.0;
                    double platformFee = booking.getPlatformFee() != null ? booking.getPlatformFee() : 0.0;
                    double ownerEarnings = deposit - platformFee;

                    if (ownerEarnings > 0) {
                        UUID ownerId = booking.getService().getOwner().getUserId();
                        walletService.addFunds(ownerId, ownerEarnings);
                    }

                    double totalPaid = booking.getDepositAmount() != null ? booking.getDepositAmount() : 0.0;
                    int earnedPoints = (int) (totalPaid / 10000);   //10.000 VND = 1 point

                    if (earnedPoints > 0) {
                        User customer = booking.getCustomer();
                        int currentPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;
                        customer.setTotalPoints(currentPoints + earnedPoints);
                        userRepository.save(customer);
                    }
                    notificationService.notifyPaymentSuccess(bookingId);
                }
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}