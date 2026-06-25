package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.config.SePayConfig;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final SePayConfig config;
    private final BookingRepository bookingRepository;
    private final String CLIENT_URL = "http://localhost:3000"; //TODO: Update to deployed client url

    @Autowired
    public PaymentService(SePayConfig config, BookingRepository bookingRepository) {
        this.config = config;
        this.bookingRepository = bookingRepository;
    }

    public Map<String, Object> generatePaymentData(UUID bookingId, UUID customerId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền thanh toán cho đơn này!");
        }

        Map<String, String> fields = new HashMap<>();
        fields.put("merchant", config.merchantId);
        fields.put("currency", "VND");
        fields.put("order_amount", String.valueOf(Math.round(booking.getDepositAmount())));
        fields.put("operation", "PURCHASE");
        fields.put("order_description", "Thanh toan coc dat lich " + bookingId.toString().substring(0, 8));
        fields.put("order_invoice_number", bookingId.toString());
        fields.put("customer_id", customerId.toString());
        fields.put("success_url", CLIENT_URL + "/payment/success");
        fields.put("error_url", CLIENT_URL + "/payment/error");
        fields.put("cancel_url", CLIENT_URL + "/payment/cancel");

        String signature = config.generateSignature(fields);
        fields.put("signature", signature);

        Map<String, Object> response = new HashMap<>();
        response.put("actionUrl", config.payUrl);
        response.put("fields", fields);
        return response;
    }
}