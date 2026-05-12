package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.common.enums.VerificationType;
import com.example.tmdt_bookingmakeup_app.dto.request.auth.VerifyRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.auth.VerifyResponse;
import com.example.tmdt_bookingmakeup_app.services.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify")
public class VerificationController {
    private final VerificationService verificationService;

    @Autowired
    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/send/reset-password")
    public ResponseEntity<VerifyResponse> sendResetPassword(@RequestBody String email) {
        VerifyResponse response = verificationService.sendVerificationCode(email, VerificationType.RESET_PASSWORD);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/account")
    public ResponseEntity<VerifyResponse> sendVerifyAccount(@RequestBody String email) {
        VerifyResponse response = verificationService.sendVerificationCode(email, VerificationType.VERIFY_USER);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<VerifyResponse> resetPassword(@RequestBody VerifyRequest request) {
        VerifyResponse response = verificationService.verifyResetPassword(request.email(), request.code(), request.newPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account")
    public ResponseEntity<VerifyResponse> verifyAccount(@RequestBody VerifyRequest request) {
        VerifyResponse response = verificationService.verifyUser(request.email(), request.code());
        return ResponseEntity.ok(response);
    }
}
