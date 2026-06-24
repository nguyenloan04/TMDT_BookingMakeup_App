package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.example.tmdt_bookingmakeup_app.services.payment.PaymentService;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    PaymentService paymentService;
    VNPayConfig config;

    @Autowired
    public PaymentController(PaymentService paymentService, VNPayConfig config) {
        this.paymentService = paymentService;
        this.config = config;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generatePayment(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        try {
            UUID customerId = UUID.fromString(rawUserId);
            UUID bookingId = UUID.fromString(payload.get("bookingId"));
            String bankCode = payload.get("bankCode");
            String locale = payload.get("locale");

            String result = paymentService.generatePayment(bookingId, customerId, bankCode, locale);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Invalid Request\"}");
        }
    }

    @GetMapping("/result")
    public ResponseEntity<String> getPaymentResult(@RequestParam Map<String, String> params) {
        JsonObject json = new JsonObject();

        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            String signValue = config.hashAllFields(fields);
            if (signValue.equals(vnp_SecureHash)) {
                String responseCode = params.get("vnp_ResponseCode");

                if ("00".equals(responseCode)) {
                    // TODO: Update payment status here
                    json.addProperty("result", true);
                    json.addProperty("message", "Payment success");
                } else {
                    json.addProperty("result", false);
                    json.addProperty("message", "Payment failed, error code: " + responseCode);
                }
            } else {
                json.addProperty("result", false);
                json.addProperty("message", "Sign invalid");
            }

        } catch (Exception e) {
            json.addProperty("result", false);
            json.addProperty("message", "Error: " + e.getMessage());
        }

        return ResponseEntity.ok(json.toString());
    }
}
