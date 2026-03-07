package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.booking.entity.Booking.DeliverySpeed;
import com.example.booking.entity.Booking.PackagingPreference;
import com.example.booking.entity.Booking.ParcelSize;
import com.example.booking.entity.Booking.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookingRequest {

    // Sender info (read-only in UI for customer scenario)
    @NotBlank
    @Size(max = 50)
    private String senderName;

    @NotBlank
    @Size(max = 200)
    private String senderAddress;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Sender contact must be 7-15 digits with optional +")
    private String senderContact;

    // Receiver info
    @NotBlank
    @Size(max = 50)
    private String receiverName;

    @NotBlank
    @Size(max = 200)
    private String receiverAddress;

    @NotBlank
    @Pattern(regexp = "^\\d{5,6}$", message = "Pin code must be 5-6 digits")
    private String receiverPinCode;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Receiver contact must be 7-15 digits with optional +")
    private String receiverContact;

    // Parcel details
    @NotNull
    private ParcelSize parcelSize;

    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @Digits(integer = 5, fraction = 2, message = "Weight can have up to 2 decimal places")
    private BigDecimal weightKg;

    @NotBlank
    @Size(max = 255)
    private String contentsDescription;

    // Shipping options
    @NotNull
    private DeliverySpeed deliverySpeed;

    @NotNull
    private PackagingPreference packagingPreference;

    // Scheduling
    @NotNull
    private LocalDateTime preferredPickup;

    // Cost & payment
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Cost can have up to 2 decimal places")
    private BigDecimal serviceCost;

    @NotNull
    private PaymentMethod paymentMethod;

    // Extras
    private boolean insuranceSelected;
    private boolean trackingEnabled = true;

    public BookingRequest() {
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverPinCode() {
        return receiverPinCode;
    }

    public void setReceiverPinCode(String receiverPinCode) {
        this.receiverPinCode = receiverPinCode;
    }

    public String getReceiverContact() {
        return receiverContact;
    }

    public void setReceiverContact(String receiverContact) {
        this.receiverContact = receiverContact;
    }

    public ParcelSize getParcelSize() {
        return parcelSize;
    }

    public void setParcelSize(ParcelSize parcelSize) {
        this.parcelSize = parcelSize;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getContentsDescription() {
        return contentsDescription;
    }

    public void setContentsDescription(String contentsDescription) {
        this.contentsDescription = contentsDescription;
    }

    public DeliverySpeed getDeliverySpeed() {
        return deliverySpeed;
    }

    public void setDeliverySpeed(DeliverySpeed deliverySpeed) {
        this.deliverySpeed = deliverySpeed;
    }

    public PackagingPreference getPackagingPreference() {
        return packagingPreference;
    }

    public void setPackagingPreference(PackagingPreference packagingPreference) {
        this.packagingPreference = packagingPreference;
    }

    public LocalDateTime getPreferredPickup() {
        return preferredPickup;
    }

    public void setPreferredPickup(LocalDateTime preferredPickup) {
        this.preferredPickup = preferredPickup;
    }

    public BigDecimal getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(BigDecimal serviceCost) {
        this.serviceCost = serviceCost;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isInsuranceSelected() {
        return insuranceSelected;
    }

    public void setInsuranceSelected(boolean insuranceSelected) {
        this.insuranceSelected = insuranceSelected;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }
}
