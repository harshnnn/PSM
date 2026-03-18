package com.example.tracking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TrackingResponse {

    private String bookingId;
    private Long originalBookingId;
    private String customerId;
    private String receiverName;
    private BigDecimal amount;
    private String trackingStatus;
    private LocalDateTime shippedAt;
    private LocalDateTime lastUpdatedAt;

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public Long getOriginalBookingId() { return originalBookingId; }
    public void setOriginalBookingId(Long originalBookingId) { this.originalBookingId = originalBookingId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTrackingStatus() { return trackingStatus; }
    public void setTrackingStatus(String trackingStatus) { this.trackingStatus = trackingStatus; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
