package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.example.tmdt_bookingmakeup_app.services.payment.PaymentIPNService;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class PaymentIPN {
    PaymentIPNService paymentIPNService = new PaymentIPNService();

    @GetMapping("/vnpay_ipn")
    public ResponseEntity<String> createPaymentIPN(@RequestParam Map<String, String> allParams) {
        return ResponseEntity.ok(paymentIPNService.generatePaymentIpn(allParams));
    }
}
