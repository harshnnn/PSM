package com.example.auth.dto;

public class AdminUserActionResponse {
    private String message;
    private String customerUsername;
    private boolean accountLocked;

    public AdminUserActionResponse() {
    }

    public AdminUserActionResponse(String message, String customerUsername, boolean accountLocked) {
        this.message = message;
        this.customerUsername = customerUsername;
        this.accountLocked = accountLocked;
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

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
}
