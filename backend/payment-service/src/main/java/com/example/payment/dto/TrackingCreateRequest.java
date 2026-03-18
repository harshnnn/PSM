package com.example.payment.dto;

import java.math.BigDecimal;

public class TrackingCreateRequest {
    private Long originalBookingId;
    private String customerId;
    private String receiverName;
    private BigDecimal amount;

    public Long getOriginalBookingId() { return originalBookingId; }
    public void setOriginalBookingId(Long originalBookingId) { this.originalBookingId = originalBookingId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
