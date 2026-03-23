package com.example.auth.dto;

public class LoginResponse {
    private String message;
    private String role;
    private String username;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String message, String role, String username, String token) {
        this.message = message;
        this.role = role;
        this.username = username;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
