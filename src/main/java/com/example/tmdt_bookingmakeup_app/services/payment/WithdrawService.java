package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.WithdrawStatus;
import com.example.tmdt_bookingmakeup_app.models.payment.Wallet;
import com.example.tmdt_bookingmakeup_app.models.payment.Withdraw;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import com.example.tmdt_bookingmakeup_app.repositories.WithdrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawService {
    private WithdrawRepository withdrawRepository;
    private ServiceOwnerRepository serviceOwnerRepository;
    private WalletService walletService;

    @Transactional
    public Withdraw requestWithdraw(UUID ownerId, Double amount) {
        ServiceOwner owner = serviceOwnerRepository.findById(ownerId).orElseThrow();
        Wallet ownerWallet = walletService.getOrCreateWallet(ownerId);

        if (ownerWallet.getBankAccountInfo() == null || ownerWallet.getBankAccountInfo().isEmpty()) {
            throw new RuntimeException("Vui lòng cập nhật thông tin Ngân hàng trước khi rút tiền!");
        }

        // Trừ tiền trong ví ngay lập tức (đóng băng số dư)
        // Nếu không đủ tiền, WalletService sẽ tự động văng lỗi và rollback
        walletService.deductFunds(ownerId, amount);

        // Tạo lệnh
        Withdraw request = new Withdraw();
        request.setOwner(owner);
        request.setAmount(amount);
        request.setStatus(WithdrawStatus.PENDING);
        request.setBankAccountInfo(ownerWallet.getBankAccountInfo()); // Snapshot số tài khoản hiện tại
        return withdrawRepository.save(request);
    }

    @Transactional
    public Withdraw approveRequest(UUID requestId, String transactionCode) {
        Withdraw request = withdrawRepository.findById(requestId).orElseThrow();

        if (request.getStatus() != WithdrawStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt yêu cầu đang chờ xử lý (PENDING)!");
        }

        request.setStatus(WithdrawStatus.APPROVED);
        request.setNote("Đã chuyển khoản thành công. Mã GD: " + transactionCode);
        return withdrawRepository.save(request);
    }

    @Transactional
    public Withdraw rejectRequest(UUID requestId, String reason) {
        Withdraw request = withdrawRepository.findById(requestId).orElseThrow();

        if (request.getStatus() != WithdrawStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể từ chối yêu cầu đang chờ xử lý (PENDING)!");
        }

        request.setStatus(WithdrawStatus.REJECTED);
        request.setNote("Từ chối: " + reason);
        withdrawRepository.save(request);

        walletService.addFunds(request.getOwner().getUserId(), request.getAmount());

        return request;
    }
}