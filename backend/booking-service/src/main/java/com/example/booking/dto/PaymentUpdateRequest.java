package com.example.booking.dto;

import com.example.booking.entity.Booking.BookingStatus;
import com.example.booking.entity.Booking.PaymentStatus;

import jakarta.validation.constraints.NotNull;

public class PaymentUpdateRequest {

    @NotNull
    private PaymentStatus paymentStatus;

    private BookingStatus bookingStatus;

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
}
