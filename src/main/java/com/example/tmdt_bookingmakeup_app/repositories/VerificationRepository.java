package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.common.enums.VerificationType;
import com.example.tmdt_bookingmakeup_app.models.auth.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    Verification getByEmail(String email);
    Verification findByEmailAndType(String email, VerificationType type);
    Verification getByEmailAndCode(String email, String code);
    Verification getByEmailAndType(String email, VerificationType type);
}
