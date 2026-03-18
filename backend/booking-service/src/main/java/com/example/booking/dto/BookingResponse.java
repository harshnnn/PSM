package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.booking.entity.Booking.BookingStatus;
import com.example.booking.entity.Booking.DeliverySpeed;
import com.example.booking.entity.Booking.PackagingPreference;
import com.example.booking.entity.Booking.ParcelSize;
import com.example.booking.entity.Booking.PaymentStatus;

public class BookingResponse {

    private Long id;
    private String customerId;
    private String senderName;
    private String receiverName;
    private String receiverPinCode;
    private ParcelSize parcelSize;
    private BigDecimal weightKg;
    private DeliverySpeed deliverySpeed;
    private PackagingPreference packagingPreference;
    private String contentsDescription;
    private PaymentStatus paymentStatus;
    private BookingStatus bookingStatus;
    private BigDecimal serviceCost;
    private LocalDateTime preferredPickup;
    private LocalDateTime createdAt;
    private String receiverAddress; // Expose receiver address
    private String receiverContact; // Expose receiver contact

    public BookingResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public String getContentsDescription() {
        return contentsDescription;
    }

    public void setContentsDescription(String contentsDescription) {
        this.contentsDescription = contentsDescription;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(BigDecimal serviceCost) {
        this.serviceCost = serviceCost;
    }

    public LocalDateTime getPreferredPickup() {
        return preferredPickup;
    }

    public void setPreferredPickup(LocalDateTime preferredPickup) {
        this.preferredPickup = preferredPickup;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
