package com.example.bookinghistory.dto;

import com.example.bookinghistory.entity.BookingHistory.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingHistoryDto {
    private Long id;
    private String customerId;
    private String bookingId;
    private String trackingBookingId;
    private LocalDateTime bookingDate;
    private String receiverName;
    private String deliveredAddress;
    private BigDecimal amount;
    private BookingStatus status;
    private String trackingStatus;

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

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTrackingBookingId() {
        return trackingBookingId;
    }

    public void setTrackingBookingId(String trackingBookingId) {
        this.trackingBookingId = trackingBookingId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getDeliveredAddress() {
        return deliveredAddress;
    }

    public void setDeliveredAddress(String deliveredAddress) {
        this.deliveredAddress = deliveredAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }
}
