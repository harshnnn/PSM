package com.example.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InvoiceRequest {

    @NotNull
    private Long bookingId;

    @NotBlank
    @Size(max = 50)
    private String customerId;

    @NotBlank
    @Size(max = 80)
    private String receiverName;

    @NotBlank
    @Size(max = 200)
    private String receiverAddress;

    @NotBlank
    @Size(max = 10)
    private String receiverPin;

    @NotBlank
    @Size(max = 20)
    private String receiverMobile;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal parcelWeightGrams;

    @NotBlank
    @Size(max = 255)
    private String contentsDescription;

    @NotBlank
    @Size(max = 20)
    private String parcelDeliveryType;

    @NotBlank
    @Size(max = 20)
    private String parcelPackingPreference;

    @NotNull
    private LocalDateTime parcelPickupTime;

    private LocalDateTime parcelDropoffTime;

    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal parcelServiceCost;

    @NotNull
    private LocalDateTime paymentTime;

    @NotBlank
    @Size(max = 12)
    private String paymentMode;

    @NotBlank
    @Size(max = 30)
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
