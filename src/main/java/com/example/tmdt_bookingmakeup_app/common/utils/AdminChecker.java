package com.example.tmdt_bookingmakeup_app.common.utils;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminChecker {
    UserService userService;

    public boolean isAdmin(HttpServletRequest request) {
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
