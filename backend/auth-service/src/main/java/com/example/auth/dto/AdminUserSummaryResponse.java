package com.example.auth.dto;

public class AdminUserSummaryResponse {
    private String customerUsername;
    private String customerName;
    private String email;
    private String countryCode;
    private String mobileNumber;
    private String role;
    private boolean accountLocked;

    public AdminUserSummaryResponse() {
    }

    public AdminUserSummaryResponse(
            String customerUsername,
            String customerName,
            String email,
            String countryCode,
            String mobileNumber,
            String role,
            boolean accountLocked) {
        this.customerUsername = customerUsername;
        this.customerName = customerName;
        this.email = email;
        this.countryCode = countryCode;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.accountLocked = accountLocked;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
}
