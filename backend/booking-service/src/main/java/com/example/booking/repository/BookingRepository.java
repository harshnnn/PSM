package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.booking.entity.Booking;
import com.example.booking.entity.Booking.BookingStatus;
import com.example.booking.entity.Booking.PaymentStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBySenderNameContainingIgnoreCase(String senderName);
    List<Booking> findByReceiverPinCode(String receiverPinCode);
    List<Booking> findByBookingStatus(BookingStatus status);
    List<Booking> findByPaymentStatus(PaymentStatus status);
    List<Booking> findByCustomerIdAndPaymentStatus(String customerId, PaymentStatus status);
    List<Booking> findByPreferredPickupBetween(LocalDateTime start, LocalDateTime end);
}