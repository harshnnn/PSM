package com.example.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.booking.entity.BookingHistoryRecord;

public interface BookingHistoryRepository extends JpaRepository<BookingHistoryRecord, Long> {
    boolean existsByBookingId(String bookingId);
}