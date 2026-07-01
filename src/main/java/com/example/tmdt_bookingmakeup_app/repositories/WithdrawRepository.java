package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.payment.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WithdrawRepository extends JpaRepository<Withdraw, UUID> {

}
