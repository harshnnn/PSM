package com.example.payment.dto;

import java.math.BigDecimal;

import com.example.payment.entity.Payment.PaymentMode;
import com.example.payment.entity.Payment.PaymentStatus;

public class PaymentResponse {

    private Long bookingId;
    private Long invoiceId;
    private String invoiceNumber;
    private String trackingNumber;
    private String customerId;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private PaymentStatus status;
    private String transactionRef;
    private String message;

    public PaymentResponse() {}

    public PaymentResponse(Long bookingId, Long invoiceId, String invoiceNumber, String trackingNumber, String customerId, BigDecimal amount, PaymentMode paymentMode,
                           PaymentStatus status, String transactionRef, String message) {
        this.bookingId = bookingId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.trackingNumber = trackingNumber;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.status = status;
        this.transactionRef = transactionRef;
        this.message = message;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
