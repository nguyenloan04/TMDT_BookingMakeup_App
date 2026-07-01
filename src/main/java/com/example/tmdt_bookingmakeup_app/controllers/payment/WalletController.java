package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.dto.request.payment.BankInfoRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.payment.WalletResponse;
import com.example.tmdt_bookingmakeup_app.models.payment.Wallet;
import com.example.tmdt_bookingmakeup_app.services.payment.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // 1. LẤY THÔNG TIN VÍ
    @GetMapping("/my-wallet")
    public ResponseEntity<WalletResponse> getMyWallet(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized: Missing User Session");
        }

        try {
            UUID ownerId = UUID.fromString(rawUserId);
            Wallet wallet = walletService.getOrCreateWallet(ownerId);

            WalletResponse response = new WalletResponse(
                    wallet.getId(),
                    wallet.getBalance(),
                    wallet.getBankId() != null ? wallet.getBankId() : "",
                    wallet.getAccountNo() != null ? wallet.getAccountNo() : "",
                    wallet.getAccountName() != null ? wallet.getAccountName() : "",
                    "Lấy thông tin ví thành công"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 2. CẬP NHẬT THÔNG TIN NGÂN HÀNG
    @PostMapping("/bank-info")
    public ResponseEntity<WalletResponse> updateBankInfo(
            @RequestBody BankInfoRequest payload,
            HttpServletRequest request) {

        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized: Missing User Session");
        }

        try {
            UUID ownerId = UUID.fromString(rawUserId);

            if (payload.bankId() == null || payload.accountNo() == null || payload.accountName() == null ||
                    payload.bankId().trim().isEmpty() || payload.accountNo().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng điền đầy đủ Mã ngân hàng, Số tài khoản và Tên chủ tài khoản");
            }

            Wallet updatedWallet = walletService.updateBankInfo(
                    ownerId,
                    payload.bankId().trim(),
                    payload.accountNo().trim(),
                    payload.accountName().trim().toUpperCase()
            );

            WalletResponse response = new WalletResponse(
                    updatedWallet.getId(),
                    updatedWallet.getBalance(),
                    updatedWallet.getBankId(),
                    updatedWallet.getAccountNo(),
                    updatedWallet.getAccountName(),
                    "Cập nhật thông tin ngân hàng thành công"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}