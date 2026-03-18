package com.example.bookinghistory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class BookingHistoryBackfill {

    private static final Logger log = LoggerFactory.getLogger(BookingHistoryBackfill.class);

    private final JdbcTemplate jdbcTemplate;

    public BookingHistoryBackfill(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void backfillFromBookings() {
        // Insert missing history rows from existing bookings so officer view has data.
        // Note: column names use Spring/Hibernate physical naming (snake_case)
        String sql = """
            INSERT INTO booking_history (customer_id, booking_id, booking_date, receiver_name, delivered_address, amount, status)
            SELECT b.sender_name,
                   CONCAT('BKG-', b.id),
                   b.created_at,
                   b.receiver_name,
                   b.receiver_address,
                   b.service_cost,
                   CASE b.booking_status
                        WHEN 'CONFIRMED' THEN 'CONFIRMED'
                        WHEN 'CANCELLED' THEN 'CANCELLED'
                        ELSE 'PENDING'
                   END
                        FROM bookings b
                        WHERE b.payment_status = 'PAID'
                            AND NOT EXISTS (
                                SELECT 1 FROM booking_history h WHERE h.booking_id = CONCAT('BKG-', b.id)
                        )
            """;
        try {
            int inserted = jdbcTemplate.update(sql);
            log.info("Booking history backfill inserted {} record(s) from bookings table", inserted);
        } catch (Exception ex) {
            log.warn("Booking history backfill skipped: {}", ex.getMessage());
        }
    }
}