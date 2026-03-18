package com.example.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceCreateRequest {
    private Long bookingId;
    private String customerId;
    private String receiverName;
    private String receiverAddress;
    private String receiverPin;
    private String receiverMobile;
    private BigDecimal parcelWeightGrams;
    private String contentsDescription;
    private String parcelDeliveryType;
    private String parcelPackingPreference;
    private LocalDateTime parcelPickupTime;
    private LocalDateTime parcelDropoffTime;
    private BigDecimal parcelServiceCost;
    private LocalDateTime paymentTime;
    private String paymentMode;
    private String transactionRef;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getReceiverPin() { return receiverPin; }
    public void setReceiverPin(String receiverPin) { this.receiverPin = receiverPin; }
    public String getReceiverMobile() { return receiverMobile; }
    public void setReceiverMobile(String receiverMobile) { this.receiverMobile = receiverMobile; }
    public BigDecimal getParcelWeightGrams() { return parcelWeightGrams; }
    public void setParcelWeightGrams(BigDecimal parcelWeightGrams) { this.parcelWeightGrams = parcelWeightGrams; }
    public String getContentsDescription() { return contentsDescription; }
    public void setContentsDescription(String contentsDescription) { this.contentsDescription = contentsDescription; }
    public String getParcelDeliveryType() { return parcelDeliveryType; }
    public void setParcelDeliveryType(String parcelDeliveryType) { this.parcelDeliveryType = parcelDeliveryType; }
    public String getParcelPackingPreference() { return parcelPackingPreference; }
    public void setParcelPackingPreference(String parcelPackingPreference) { this.parcelPackingPreference = parcelPackingPreference; }
    public LocalDateTime getParcelPickupTime() { return parcelPickupTime; }
    public void setParcelPickupTime(LocalDateTime parcelPickupTime) { this.parcelPickupTime = parcelPickupTime; }
    public LocalDateTime getParcelDropoffTime() { return parcelDropoffTime; }
    public void setParcelDropoffTime(LocalDateTime parcelDropoffTime) { this.parcelDropoffTime = parcelDropoffTime; }
    public BigDecimal getParcelServiceCost() { return parcelServiceCost; }
    public void setParcelServiceCost(BigDecimal parcelServiceCost) { this.parcelServiceCost = parcelServiceCost; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
}
