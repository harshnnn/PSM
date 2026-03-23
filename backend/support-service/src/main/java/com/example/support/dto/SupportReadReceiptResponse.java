package com.example.support.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SupportReadReceiptResponse {

    private String customerUsername;
    private String readByUsername;
    private String readByRole;
    private LocalDateTime readAt;
    private List<Long> messageIds = new ArrayList<>();

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }

    public String getReadByUsername() {
        return readByUsername;
    }

    public void setReadByUsername(String readByUsername) {
        this.readByUsername = readByUsername;
    }

    public String getReadByRole() {
        return readByRole;
    }

    public void setReadByRole(String readByRole) {
        this.readByRole = readByRole;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }
}
