package com.example.invoice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false, length = 50)
    private String customerId;

    @Column(nullable = false, length = 80)
    private String receiverName;

    @Column(nullable = false, length = 200)
    private String receiverAddress;

    @Column(nullable = false, length = 10)
    private String receiverPin;

    @Column(nullable = false, length = 20)
    private String receiverMobile;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal parcelWeightGrams;

    @Column(nullable = false, length = 255)
    private String contentsDescription;

    @Column(nullable = false, length = 20)
    private String parcelDeliveryType;

    @Column(nullable = false, length = 20)
    private String parcelPackingPreference;

    @Column(nullable = false)
    private LocalDateTime parcelPickupTime;

    @Column
    private LocalDateTime parcelDropoffTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal parcelServiceCost;

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    @Column(nullable = false, length = 12)
    private String paymentMode;

    @Column(nullable = false, length = 30)
    private String transactionRef;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
