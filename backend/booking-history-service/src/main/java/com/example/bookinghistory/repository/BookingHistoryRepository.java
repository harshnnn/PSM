package com.example.bookinghistory.repository;

import com.example.bookinghistory.entity.BookingHistory;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    Page<BookingHistory> findByCustomerIdOrderByBookingDateDesc(String customerId, Pageable pageable);

    Page<BookingHistory> findByCustomerIdAndBookingDateBetweenOrderByBookingDateDesc(
            String customerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<BookingHistory> findByBookingDateBetweenOrderByBookingDateDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
