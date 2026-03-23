package com.example.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.auth.dto.AdminUserActionResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.AdminUserSummaryResponse;
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
    public ResponseEntity<ProfileResponse> profile(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-Username", required = false) String authenticatedUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        enforceSelfProfileAccess(customerUsername, authenticatedUsername, authenticatedRole);
        return ResponseEntity.ok(authService.getProfile(customerUsername));
    }

    @PutMapping("/profile/{customerUsername}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-Username", required = false) String authenticatedUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole,
            @Valid @RequestBody UpdateProfileRequest request) {
        enforceSelfProfileAccess(customerUsername, authenticatedUsername, authenticatedRole);
        return ResponseEntity.ok(authService.updateProfile(customerUsername, request));
    }

    @PutMapping("/profile/{customerUsername}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-Username", required = false) String authenticatedUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole,
            @Valid @RequestBody ChangePasswordRequest request) {
        enforceSelfProfileAccess(customerUsername, authenticatedUsername, authenticatedRole);
        authService.changePassword(customerUsername, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<AdminUserSummaryResponse>> getAdminUsers(
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        enforceOfficerAccess(authenticatedRole);
        return ResponseEntity.ok(authService.getManageableUsers());
    }

    @PutMapping("/admin/users/{customerUsername}/lock")
    public ResponseEntity<AdminUserActionResponse> lockUserAccount(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        enforceOfficerAccess(authenticatedRole);
        return ResponseEntity.ok(authService.lockUserAccount(customerUsername));
    }

    @PutMapping("/admin/users/{customerUsername}/unlock")
    public ResponseEntity<AdminUserActionResponse> unlockUserAccount(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        enforceOfficerAccess(authenticatedRole);
        return ResponseEntity.ok(authService.unlockUserAccount(customerUsername));
    }

    @DeleteMapping("/admin/users/{customerUsername}")
    public ResponseEntity<AdminUserActionResponse> deleteUserAccount(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        enforceOfficerAccess(authenticatedRole);
        return ResponseEntity.ok(authService.deleteUserAccount(customerUsername));
    }

    private void enforceSelfProfileAccess(String customerUsername, String authenticatedUsername, String authenticatedRole) {
        if (!"CUSTOMER".equalsIgnoreCase(authenticatedRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only authenticated customers can access profile endpoints");
        }

        if (authenticatedUsername == null || !customerUsername.equalsIgnoreCase(authenticatedUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own profile");
        }
    }

    private void enforceOfficerAccess(String authenticatedRole) {
        if (!"OFFICER".equalsIgnoreCase(authenticatedRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin officers can access this endpoint");
        }
    }
}
