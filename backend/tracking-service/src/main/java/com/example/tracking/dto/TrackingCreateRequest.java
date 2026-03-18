package com.example.tracking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TrackingCreateRequest {

    @NotNull
    private Long originalBookingId;

    @NotBlank
    @Size(max = 50)
    private String customerId;

    @Size(max = 80)
    private String receiverName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
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
