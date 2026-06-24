package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.services.payment.PaymentIPNService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PaymentIPN {
    private final PaymentIPNService paymentIPNService;

    // Update status to BookingStatus.PAID_DEPOSIT
    @GetMapping("/vnpay_ipn")
    public ResponseEntity<String> createPaymentIPN(@RequestParam Map<String, String> allParams) {
        return ResponseEntity.ok(paymentIPNService.generatePaymentIpn(allParams));
    }
}