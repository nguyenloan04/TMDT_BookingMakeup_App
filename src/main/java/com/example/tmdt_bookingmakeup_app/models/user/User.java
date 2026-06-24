package com.example.tmdt_bookingmakeup_app.models.user;

import com.example.tmdt_bookingmakeup_app.common.enums.Gender;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "gender")
    private Gender gender;

    @Column(name = "role")
    private UserRole role;

    @Column(name = "active", nullable = false)
    private boolean isActive;

    @Column(name = "verified", nullable = false)
    private boolean isVerified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "address")
    private String address;

    @Column(name="total_points")
    private Integer totalPoints;

    @PrePersist
    protected void onCreate() {
        // FIXME: add default avatar url later.
        avatarUrl = "";
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        role = UserRole.USER;
        totalPoints = 0;
    }
}
