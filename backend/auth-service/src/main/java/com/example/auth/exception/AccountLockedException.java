package com.example.auth.exception;

public class AccountLockedException extends RuntimeException {
    private final String supportEmail;

    public AccountLockedException(String message, String supportEmail) {
        super(message);
        this.supportEmail = supportEmail;
    }

    public String getSupportEmail() {
        return supportEmail;
    }
}
