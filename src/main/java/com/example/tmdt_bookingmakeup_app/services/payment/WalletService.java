package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.models.payment.Wallet;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import com.example.tmdt_bookingmakeup_app.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private WalletRepository walletRepository;
    private UserRepository userRepository;

    // HÀM LAZY INIT - Lấy ví, không có thì tạo ngay lập tức
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Wallet getOrCreateWallet(UUID ownerId) {
        return walletRepository.findByOwnerId(ownerId)
                .orElseGet(() -> {
                    User owner = userRepository.findById(ownerId)
                            .orElseThrow(() -> new RuntimeException("Chủ dịch vụ không tồn tại"));

                    Wallet newWallet = new Wallet();
                    newWallet.setOwner(owner);
                    newWallet.setBalance(0.0);
                    return walletRepository.save(newWallet);
                });
    }

    @Transactional
    public void addFunds(UUID ownerId, Double amount) {
        if (amount <= 0) return;
        Wallet wallet = getOrCreateWallet(ownerId);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
    }

    @Transactional
    public void deductFunds(UUID ownerId, Double amount) {
        Wallet wallet = getOrCreateWallet(ownerId);
        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Số dư khả dụng không đủ!");
        }
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);
    }
}