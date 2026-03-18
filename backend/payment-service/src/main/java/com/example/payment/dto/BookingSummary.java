package com.example.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingSummary {
    private Long id;
    private String customerId;
    private BigDecimal serviceCost;
    private String paymentStatus;
    private String bookingStatus;
    private LocalDateTime createdAt;
    private String receiverName;
    private String receiverAddress;
    private String receiverPinCode;
    private String receiverContact;
    private String contentsDescription;
    private String parcelSize;
    private String deliverySpeed;
    private String packagingPreference;
    private BigDecimal weightKg;
    private LocalDateTime preferredPickup;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getServiceCost() { return serviceCost; }
    public void setServiceCost(BigDecimal serviceCost) { this.serviceCost = serviceCost; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getReceiverPinCode() { return receiverPinCode; }
    public void setReceiverPinCode(String receiverPinCode) { this.receiverPinCode = receiverPinCode; }
    public String getReceiverContact() { return receiverContact; }
    public void setReceiverContact(String receiverContact) { this.receiverContact = receiverContact; }
    public String getContentsDescription() { return contentsDescription; }
    public void setContentsDescription(String contentsDescription) { this.contentsDescription = contentsDescription; }
    public String getParcelSize() { return parcelSize; }
    public void setParcelSize(String parcelSize) { this.parcelSize = parcelSize; }
    public String getDeliverySpeed() { return deliverySpeed; }
    public void setDeliverySpeed(String deliverySpeed) { this.deliverySpeed = deliverySpeed; }
    public String getPackagingPreference() { return packagingPreference; }
    public void setPackagingPreference(String packagingPreference) { this.packagingPreference = packagingPreference; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public LocalDateTime getPreferredPickup() { return preferredPickup; }
    public void setPreferredPickup(LocalDateTime preferredPickup) { this.preferredPickup = preferredPickup; }
}
