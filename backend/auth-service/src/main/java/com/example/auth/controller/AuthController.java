package com.example.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.ProfileResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.RegisterResponse;
import com.example.auth.dto.UpdateProfileRequest;
import com.example.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile/{customerUsername}")
    public ResponseEntity<ProfileResponse> profile(@PathVariable String customerUsername) {
        return ResponseEntity.ok(authService.getProfile(customerUsername));
    }

    @PutMapping("/profile/{customerUsername}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable String customerUsername,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(customerUsername, request));
    }

    @PutMapping("/profile/{customerUsername}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String customerUsername,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(customerUsername, request);
        return ResponseEntity.noContent().build();
    }
}
