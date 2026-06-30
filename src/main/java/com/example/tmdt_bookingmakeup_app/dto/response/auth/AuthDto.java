package com.example.tmdt_bookingmakeup_app.dto.response.auth;

import com.example.tmdt_bookingmakeup_app.common.enums.Gender;
import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthDto {
    private UUID id;
    private String email;
    private String username;
    private String displayName;
    private Gender gender;
    private String avatar;
    private String phone;
    private String description;
    private boolean isActive;
    private boolean isVerified;
    private UserRole role;
    private String jwtToken;
    private int totalPoint;
}
