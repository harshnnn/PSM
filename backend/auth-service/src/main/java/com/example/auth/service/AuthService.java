package com.example.auth.service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth.dto.AdminUserActionResponse;
import com.example.auth.dto.AdminUserSummaryResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.ProfileResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.RegisterResponse;
import com.example.auth.dto.UpdateProfileRequest;
import com.example.auth.entity.UserAccount;
import com.example.auth.exception.AccountLockedException;
import com.example.auth.repository.UserAccountRepository;

@Service
public class AuthService {
    private static final String OFFICER_USER_ID = "officer01";
    private static final String OFFICER_PASSWORD = "Officer@123";
    private static final String OFFICER_ROLE = "OFFICER";
    private static final String CUSTOMER_ROLE = "CUSTOMER";
    private static final String SUPPORT_EMAIL = "support@pmslogistics.demo";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z]{2,})+$"
    );

    private final UserAccountRepository userAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and Confirm Password must match");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email must be in a valid format");
        }

        if (userAccountRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists");
        }

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered with another account");
        }

        String customerUsername = request.getUserId();

        UserAccount user = new UserAccount();
        user.setCustomerName(normalizeText(request.getCustomerName()));
        user.setEmail(normalizedEmail);
        user.setCountryCode(request.getCountryCode());
        user.setMobileNumber(request.getMobileNumber());
        user.setAddress(request.getAddress());
        user.setUserId(request.getUserId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(CUSTOMER_ROLE);
        user.setPreferences(request.getPreferences());
        user.setCustomerUsername(customerUsername);
        user.setAccountLocked(false);

        userAccountRepository.save(user);
        String token = jwtService.generateToken(customerUsername, user.getRole());
        return new RegisterResponse("Registration successful", customerUsername, user.getCustomerName(), user.getEmail(), token);
    }

    public LoginResponse login(LoginRequest request) {
        if (OFFICER_USER_ID.equals(request.getUserId()) && OFFICER_PASSWORD.equals(request.getPassword())) {
            String token = jwtService.generateToken(OFFICER_USER_ID, OFFICER_ROLE);
            return new LoginResponse("Login successful", OFFICER_ROLE, OFFICER_USER_ID, token);
        }

        UserAccount user = userAccountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID or password"));

        ensureAccountUnlocked(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid user ID or password");
        }

        String token = jwtService.generateToken(user.getCustomerUsername(), user.getRole());
        return new LoginResponse("Login successful", user.getRole(), user.getCustomerUsername(), token);
    }

    public List<AdminUserSummaryResponse> getManageableUsers() {
        return userAccountRepository.findAllByRoleOrderByCustomerUsernameAsc(CUSTOMER_ROLE)
                .stream()
                .map(this::toAdminUserSummary)
                .toList();
    }

    public AdminUserActionResponse lockUserAccount(String customerUsername) {
        UserAccount user = findManageableUser(customerUsername);
        user.setAccountLocked(true);
        userAccountRepository.save(user);
        return new AdminUserActionResponse("Account locked successfully", user.getCustomerUsername(), true);
    }

    public AdminUserActionResponse unlockUserAccount(String customerUsername) {
        UserAccount user = findManageableUser(customerUsername);
        user.setAccountLocked(false);
        userAccountRepository.save(user);
        return new AdminUserActionResponse("Account unlocked successfully", user.getCustomerUsername(), false);
    }

    public AdminUserActionResponse deleteUserAccount(String customerUsername) {
        UserAccount user = findManageableUser(customerUsername);
        userAccountRepository.deleteById(Objects.requireNonNull(user.getId()));
        return new AdminUserActionResponse("Account deleted successfully", customerUsername, false);
    }

    public ProfileResponse getProfile(String customerUsername) {
        UserAccount user = userAccountRepository.findByCustomerUsername(customerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        ensureAccountUnlocked(user);

        return new ProfileResponse(
                user.getCustomerUsername(),
            normalizeText(user.getCustomerName()),
                user.getAddress(),
                user.getCountryCode(),
                user.getMobileNumber(),
                user.getEmail(),
                user.getPreferences());
    }

    public ProfileResponse updateProfile(String customerUsername, UpdateProfileRequest request) {
        UserAccount user = userAccountRepository.findByCustomerUsername(customerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        ensureAccountUnlocked(user);

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

        ensureAccountUnlocked(user);

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

    private UserAccount findManageableUser(String customerUsername) {
        UserAccount user = userAccountRepository.findByCustomerUsername(customerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User account not found"));

        if (!CUSTOMER_ROLE.equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("Only customer accounts can be managed");
        }

        return user;
    }

    private AdminUserSummaryResponse toAdminUserSummary(UserAccount user) {
        return new AdminUserSummaryResponse(
                user.getCustomerUsername(),
                user.getCustomerName(),
                user.getEmail(),
                user.getCountryCode(),
                user.getMobileNumber(),
                user.getRole(),
                user.isAccountLocked()
        );
    }

    private void ensureAccountUnlocked(UserAccount user) {
        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "Your account has been locked. Please contact support for help.",
                    SUPPORT_EMAIL
            );
        }
    }
}
