package com.example.auth.dto;

public class RegisterResponse {
    private String message;
    private String customerUsername;
    private String customerName;
    private String email;
    private String token;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, String customerUsername, String customerName, String email, String token) {
        this.message = message;
        this.customerUsername = customerUsername;
        this.customerName = customerName;
        this.email = email;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
