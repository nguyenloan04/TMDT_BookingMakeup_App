package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.config.SePayConfig;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import com.example.tmdt_bookingmakeup_app.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentIPN {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SePayConfig sePayConfig;

    @Autowired
    private NotificationService notificationService;

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
                    booking.setStatus(BookingStatus.PAID_DEPOSIT);
                    bookingRepository.save(booking);
                    notificationService.notifyPaymentSuccess(bookingId);
                }
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}