package com.example.auth.dto;

public class ProfileResponse {
    private String customerUsername;
    private String customerName;
    private String address;
    private String countryCode;
    private String mobileNumber;
    private String email;

    public ProfileResponse(String customerUsername, String customerName, String address, String countryCode, String mobileNumber, String email) {
        this.customerUsername = customerUsername;
        this.customerName = customerName;
        this.address = address;
        this.countryCode = countryCode;
        this.mobileNumber = mobileNumber;
        this.email = email;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
