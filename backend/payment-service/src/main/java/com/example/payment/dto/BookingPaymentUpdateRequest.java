package com.example.payment.dto;

public class BookingPaymentUpdateRequest {
    private String paymentStatus;
    private String bookingStatus;

    public BookingPaymentUpdateRequest() {}

    public BookingPaymentUpdateRequest(String paymentStatus, String bookingStatus) {
        this.paymentStatus = paymentStatus;
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
}
