package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.request.auth.LoginRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.auth.RegisterRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.auth.AuthDto;
import com.example.tmdt_bookingmakeup_app.dto.response.auth.AuthResponse;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import com.example.tmdt_bookingmakeup_app.security.JwtConfig;
import com.example.tmdt_bookingmakeup_app.security.PasswordEncryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    @Autowired
    public AuthService(UserRepository userRepository, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.jwtConfig = jwtConfig;
    }

    public AuthResponse login(LoginRequest input) {
        User targetUser = userRepository.findByEmail(input.email());
        if (targetUser == null) {
            return new AuthResponse(false, "Wrong password or email", null);
        }
        boolean isPasswordMatch = PasswordEncryption.checkPassword(input.password(), targetUser.getPassword());
        if (!isPasswordMatch) {
            return new AuthResponse(false, "Wrong password or email", null);
        }
        int roleOrdinal = targetUser.getRole() != null ? targetUser.getRole().ordinal() : com.example.tmdt_bookingmakeup_app.common.enums.UserRole.ADMIN.ordinal();
        String jwtToken = jwtConfig.generateToken(targetUser.getId(), roleOrdinal);
        AuthDto dto = getAuthDto(targetUser, jwtToken);
        return new AuthResponse(true, "Login success!", dto);
    }

    @Transactional
    public AuthResponse register(RegisterRequest input) {
        User existUser = this.userRepository.findByEmail(input.email());
        if (existUser != null) {
            return new AuthResponse(false, "Account already exists", null);
        }
        User newUser = new User();
        newUser.setEmail(input.email());
        newUser.setUsername(input.username());
        newUser.setPassword(PasswordEncryption.hashPassword(input.password()));
        newUser.setRole(com.example.tmdt_bookingmakeup_app.common.enums.UserRole.ADMIN);
        newUser.setActive(true);
        this.userRepository.save(newUser);
        return new AuthResponse(true, "Register success!", null);
    }

    private static AuthDto getAuthDto(User targetUser, String jwtToken) {
        AuthDto dto = new AuthDto();
        dto.setId(targetUser.getId());
        dto.setActive(targetUser.isActive());
        dto.setAvatar(targetUser.getAvatarUrl());
//        dto.setDescription(targetUser.getDescription());
        dto.setDisplayName(targetUser.getDisplayName());
        dto.setEmail(targetUser.getEmail());
//        dto.setGender(targetUser.getGender());
        dto.setRole(targetUser.getRole());
        dto.setUsername(targetUser.getUsername());
        dto.setVerified(targetUser.isVerified());
        dto.setJwtToken(jwtToken);
        return dto;
    }
}
