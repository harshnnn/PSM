package com.example.auth.service;

import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.ProfileResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.RegisterResponse;
import com.example.auth.dto.UpdateProfileRequest;
import com.example.auth.entity.UserAccount;
import com.example.auth.repository.UserAccountRepository;

@Service
public class AuthService {
    private static final String OFFICER_USER_ID = "officer01";
    private static final String OFFICER_PASSWORD = "Officer@123";
    private static final String OFFICER_ROLE = "OFFICER";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z]{2,})+$"
    );

    private final UserAccountRepository userAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and Confirm Password must match");
        }

        if (userAccountRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists");
        }

        String customerUsername = request.getUserId();

        UserAccount user = new UserAccount();
        user.setCustomerName(request.getCustomerName());
        user.setEmail(request.getEmail());
        user.setCountryCode(request.getCountryCode());
        user.setMobileNumber(request.getMobileNumber());
        user.setAddress(request.getAddress());
        user.setUserId(request.getUserId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        user.setPreferences(request.getPreferences());
        user.setCustomerUsername(customerUsername);

        userAccountRepository.save(user);
        return new RegisterResponse("Registration successful", customerUsername, user.getCustomerName(), user.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        if (OFFICER_USER_ID.equals(request.getUserId()) && OFFICER_PASSWORD.equals(request.getPassword())) {
            return new LoginResponse("Login successful", OFFICER_ROLE, OFFICER_USER_ID);
        }

        UserAccount user = userAccountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid user ID or password");
        }

        return new LoginResponse("Login successful", user.getRole(), user.getCustomerUsername());
    }

    public ProfileResponse getProfile(String customerUsername) {
        UserAccount user = userAccountRepository.findByCustomerUsername(customerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        return new ProfileResponse(
                user.getCustomerUsername(),
                user.getCustomerName(),
                user.getAddress(),
                user.getCountryCode(),
                user.getMobileNumber(),
                user.getEmail(),
                user.getPreferences());
    }

    public ProfileResponse updateProfile(String customerUsername, UpdateProfileRequest request) {
        UserAccount user = userAccountRepository.findByCustomerUsername(customerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email must be in a valid format");
        }
        if (!normalizedEmail.equalsIgnoreCase(user.getEmail()) && userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered with another account");
        }

        user.setCustomerName(normalizeText(request.getCustomerName()));
        user.setEmail(normalizedEmail);
        user.setCountryCode(request.getCountryCode().trim());
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setAddress(normalizeText(request.getAddress()));
        user.setPreferences(normalizeText(request.getPreferences()));

        UserAccount saved = userAccountRepository.save(user);
        return new ProfileResponse(
                saved.getCustomerUsername(),
                saved.getCustomerName(),
                saved.getAddress(),
                saved.getCountryCode(),
                saved.getMobileNumber(),
                saved.getEmail(),
                saved.getPreferences());
    }

    public void changePassword(String customerUsername, ChangePasswordRequest request) {
        UserAccount user = userAccountRepository.findByCustomerUsername(customerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New Password and Confirm New Password must match");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
