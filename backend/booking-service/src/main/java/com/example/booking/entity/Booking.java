package com.example.booking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer identity (username/userId)
    @Column(name = "customer_id", length = 50)
    private String customerId;

    // Sender info
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String senderName;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String senderAddress;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Sender contact must be 7-15 digits with optional +")
    @Column(nullable = false, length = 20)
    private String senderContact;

    // Receiver info
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String receiverName;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String receiverAddress;

    @NotBlank
    @Pattern(regexp = "^\\d{5,6}$", message = "Pin code must be 5-6 digits")
    @Column(nullable = false, length = 6)
    private String receiverPinCode;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Receiver contact must be 7-15 digits with optional +")
    @Column(nullable = false, length = 20)
    private String receiverContact;

    // Parcel details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ParcelSize parcelSize;

    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @Digits(integer = 5, fraction = 2, message = "Weight can have up to 2 decimal places")
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal weightKg;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String contentsDescription;

    // Shipping options
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DeliverySpeed deliverySpeed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PackagingPreference packagingPreference;

    // Scheduling
    @NotNull
    @Column(nullable = false)
    private LocalDateTime preferredPickup;

    // Cost & payment
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Cost can have up to 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Extras
    @Column(nullable = false)
    private boolean insuranceSelected = false;

    @Column(nullable = false)
    private boolean trackingEnabled = true;

    // Booking lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Booking() {
    }

    public Booking(Long id, String senderName, String senderAddress, String senderContact,
                   String receiverName, String receiverAddress, String receiverPinCode, String receiverContact,
                   ParcelSize parcelSize, BigDecimal weightKg, String contentsDescription,
                   DeliverySpeed deliverySpeed, PackagingPreference packagingPreference,
                   LocalDateTime preferredPickup, BigDecimal serviceCost, PaymentMethod paymentMethod,
                   PaymentStatus paymentStatus, boolean insuranceSelected, boolean trackingEnabled,
                   BookingStatus bookingStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.senderContact = senderContact;
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.receiverPinCode = receiverPinCode;
        this.receiverContact = receiverContact;
        this.parcelSize = parcelSize;
        this.weightKg = weightKg;
        this.contentsDescription = contentsDescription;
        this.deliverySpeed = deliverySpeed;
        this.packagingPreference = packagingPreference;
        this.preferredPickup = preferredPickup;
        this.serviceCost = serviceCost;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.insuranceSelected = insuranceSelected;
        this.trackingEnabled = trackingEnabled;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ParcelSize { SMALL, MEDIUM, LARGE, CUSTOM }
    public enum DeliverySpeed { STANDARD, EXPRESS, SAME_DAY }
    public enum PackagingPreference { STANDARD, CUSTOM, ECO_FRIENDLY, FRAGILE }
    public enum PaymentMethod { CASH, CARD, UPI, WALLET }
    public enum PaymentStatus { PENDING, PAID, FAILED }
    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED }

    // Getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }
    public String getSenderContact() { return senderContact; }
    public void setSenderContact(String senderContact) { this.senderContact = senderContact; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getReceiverPinCode() { return receiverPinCode; }
    public void setReceiverPinCode(String receiverPinCode) { this.receiverPinCode = receiverPinCode; }
    public String getReceiverContact() { return receiverContact; }
    public void setReceiverContact(String receiverContact) { this.receiverContact = receiverContact; }
    public ParcelSize getParcelSize() { return parcelSize; }
    public void setParcelSize(ParcelSize parcelSize) { this.parcelSize = parcelSize; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public String getContentsDescription() { return contentsDescription; }
    public void setContentsDescription(String contentsDescription) { this.contentsDescription = contentsDescription; }
    public DeliverySpeed getDeliverySpeed() { return deliverySpeed; }
    public void setDeliverySpeed(DeliverySpeed deliverySpeed) { this.deliverySpeed = deliverySpeed; }
    public PackagingPreference getPackagingPreference() { return packagingPreference; }
    public void setPackagingPreference(PackagingPreference packagingPreference) { this.packagingPreference = packagingPreference; }
    public LocalDateTime getPreferredPickup() { return preferredPickup; }
    public void setPreferredPickup(LocalDateTime preferredPickup) { this.preferredPickup = preferredPickup; }
    public BigDecimal getServiceCost() { return serviceCost; }
    public void setServiceCost(BigDecimal serviceCost) { this.serviceCost = serviceCost; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public boolean isInsuranceSelected() { return insuranceSelected; }
    public void setInsuranceSelected(boolean insuranceSelected) { this.insuranceSelected = insuranceSelected; }
    public boolean isTrackingEnabled() { return trackingEnabled; }
    public void setTrackingEnabled(boolean trackingEnabled) { this.trackingEnabled = trackingEnabled; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}