package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z]+(?:\\s[A-Za-z]+)*$", message = "Customer name must contain letters and spaces only")
    private String customerName;

    @NotBlank
        @Pattern(
            regexp = "^(?!.*\\.\\.)[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z]{2,})+$",
            message = "Email must be in a valid format"
        )
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\+\\d{1,4}$", message = "Country code must be like +91")
    private String countryCode;

    @NotBlank
    @Pattern(regexp = "^(?!.*(\\d)\\1{5,})[6-9]\\d{9}$", message = "Mobile number must be 10 digits, start with 6, 7, 8, or 9, and cannot contain any digit repeated more than 5 times consecutively")
    private String mobileNumber;

    @NotBlank
    @Size(max = 200)
    private String address;

    @NotBlank
    @Size(min = 5, max = 20)
    private String userId;

    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,30}$", message = "Password must include upper, lower, and special character")
    private String password;

    @NotBlank
    @Size(max = 30)
    private String confirmPassword;

    @Size(max = 200)
    private String preferences;

    public RegisterRequest() {
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
}
