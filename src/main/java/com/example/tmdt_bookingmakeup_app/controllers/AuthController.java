package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.request.auth.LoginRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.auth.RegisterRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.auth.RegisterServiceOwnerRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.auth.AuthResponse;
import com.example.tmdt_bookingmakeup_app.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = this.authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        AuthResponse response = this.authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/service-owner")
    public ResponseEntity<AuthResponse> registerServiceOwner(
            @RequestBody RegisterServiceOwnerRequest registerServiceOwnerRequest
    ) {
        AuthResponse response = this.authService.registerServiceOwner(registerServiceOwnerRequest);
        return ResponseEntity.ok(response);
    }
}
