package com.example.tmdt_bookingmakeup_app.dto.response.user;

import com.example.tmdt_bookingmakeup_app.common.enums.Gender;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String phone;
    private Gender gender;
    private UserRole role;
    private boolean isActive;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String address;
    private Integer totalPoints;
}
