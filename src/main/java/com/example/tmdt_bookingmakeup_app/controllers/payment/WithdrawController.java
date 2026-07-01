package com.example.tmdt_bookingmakeup_app.controllers.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import com.example.tmdt_bookingmakeup_app.services.payment.WithdrawService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wallet/withdraws")
@RequiredArgsConstructor
public class WithdrawController {
    private final WithdrawService withdrawService;
    private final UserService userService;

    // API CHO CHỦ TIỆM: Đặt lệnh rút tiền
    @PostMapping("/request")
    public ResponseEntity<?> requestWithdraw(@RequestBody Map<String, Double> payload, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            UUID ownerId = UUID.fromString(rawUserId);
            Double amount = payload.get("amount");
            var result = withdrawService.requestWithdraw(ownerId, amount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // API CHO ADMIN: Duyệt lệnh (Kèm mã giao dịch)
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveWithdraw(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request
    ) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String transactionCode = payload.getOrDefault("transactionCode", "Không có mã");
            var result = withdrawService.approveRequest(id, transactionCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // API CHO ADMIN: Từ chối lệnh (Kèm lý do)
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectWithdraw(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request
    ) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String reason = payload.getOrDefault("reason", "Lỗi thông tin tài khoản");
            var result = withdrawService.rejectRequest(id, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    //Seperated to single file later
    private boolean isAdmin(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return false;
        }
        try {
            return userService.getUserProfile(UUID.fromString(rawUserId)).getRole() == UserRole.ADMIN;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}