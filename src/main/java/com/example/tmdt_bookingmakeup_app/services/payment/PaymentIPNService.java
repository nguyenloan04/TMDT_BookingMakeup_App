package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.common.enums.PaymentStatus;
import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import com.example.tmdt_bookingmakeup_app.services.NotificationService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentIPNService {
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Autowired
    public PaymentIPNService(BookingRepository bookingRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    public String generatePaymentIpn(Map<String, String> allParams) {
        JsonObject json = new JsonObject();
        String rspCode;
        String message;

        try {
            Map<String, String> fields = new HashMap<>(allParams);
            String vnpSecureHash = allParams.get("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Checksum
            String signValue = VNPayConfig.generateHmacSHA512(fields.keySet().toString(), fields.values().toString());

            if (signValue.equals(vnpSecureHash)) {
                // Get Booking ID
                String requestBookingId = allParams.get("vnp_TxnRef");
                UUID bookingId = UUID.fromString(requestBookingId);

                Booking booking = bookingRepository.findById(bookingId).orElse(null);

                if (booking != null) {
                    long vnpAmount = Long.parseLong(allParams.get("vnp_Amount")) / 100;
                    long dbDepositAmount = Math.round(booking.getDepositAmount());

                    if (vnpAmount == dbDepositAmount) {
                        if (booking.getStatus() == BookingStatus.PENDING) {
                            if ("00".equals(allParams.get("vnp_ResponseCode"))) {
                                booking.setStatus(BookingStatus.PAID);
                                bookingRepository.save(booking);
                                notificationService.notifyPaymentSuccess(bookingId);
                                rspCode = "00";
                                message = "Confirm Success";
                            } else {
                                rspCode = "00";
                                message = "Payment failed";
                            }
                        } else {
                            rspCode = "02";
                            message = "Booking already confirmed or paid";
                        }
                    } else {
                        rspCode = "04";
                        message = "Invalid Amount";
                    }
                } else {
                    rspCode = "01";
                    message = "Booking not Found";
                }
            } else {
                rspCode = "97";
                message = "Invalid Checksum";
            }

            json.addProperty("rspCode", rspCode);
            json.addProperty("message", message);
            return json.toString();
        } catch (Exception e) {
            json.addProperty("rspCode", "99");
            json.addProperty("message", "Unknown error");
            return json.toString();
        }
    }
}