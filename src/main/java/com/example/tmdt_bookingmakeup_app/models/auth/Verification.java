package com.example.tmdt_bookingmakeup_app.models.auth;

import com.example.tmdt_bookingmakeup_app.common.enums.VerificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int attempts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationType type;

    private LocalDateTime expiredAt;

    @PrePersist
    public void onCreate() {
        attempts = 0;
        expiredAt = LocalDateTime.now().plusMinutes(10);
    }
}
