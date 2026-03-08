package com.example.bookinghistory.controller;

import com.example.bookinghistory.dto.BookingHistoryDto;
import com.example.bookinghistory.dto.PageResponse;
import com.example.bookinghistory.entity.BookingHistory;
import com.example.bookinghistory.repository.BookingHistoryRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
public class BookingHistoryController {

    private final BookingHistoryRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public BookingHistoryController(BookingHistoryRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PageResponse<BookingHistoryDto>> customerHistory(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BookingHistory> result = repository.findByCustomerIdOrderByBookingDateDesc(customerId, pageable);
        return ResponseEntity.ok(toPageResponse(result));
    }

    @GetMapping("/officer")
    public ResponseEntity<PageResponse<BookingHistoryDto>> officerHistory(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(page, size);
        Page<BookingHistory> result;

        if (customerId != null && !customerId.isBlank()) {
            result = repository.findByCustomerIdAndBookingDateBetweenOrderByBookingDateDesc(customerId, start, end, pageable);
        } else {
            result = repository.findByBookingDateBetweenOrderByBookingDateDesc(start, end, pageable);
        }

        // Fallback: if no rows (e.g., history table empty), pull directly from bookings table so officer can see all bookings.
        if (result.isEmpty()) {
            result = fallbackFromBookings(customerId, start, end, pageable);
        }

        return ResponseEntity.ok(toPageResponse(result));
    }

    private Page<BookingHistory> fallbackFromBookings(String customerId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        StringBuilder base = new StringBuilder("SELECT id, customer_id, sender_name, created_at, receiver_name, receiver_address, service_cost, booking_status FROM bookings WHERE created_at BETWEEN ? AND ?");
        Object[] params;
        if (customerId != null && !customerId.isBlank()) {
            base.append(" AND customer_id = ?");
            params = new Object[]{start, end, customerId};
        } else {
            params = new Object[]{start, end};
        }
        base.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<BookingHistory> rows = jdbcTemplate.query(base.toString(), paramsWithPaging(params, pageable), bookingRowMapper());

        // Count total
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM bookings WHERE created_at BETWEEN ? AND ?");
        Object[] countParams;
        if (customerId != null && !customerId.isBlank()) {
            countSql.append(" AND customer_id = ?");
            countParams = new Object[]{start, end, customerId};
        } else {
            countParams = new Object[]{start, end};
        }
        long total = jdbcTemplate.queryForObject(countSql.toString(), countParams, Long.class);

        return new PageImpl<>(rows, pageable, total);
    }

    private Object[] paramsWithPaging(Object[] baseParams, Pageable pageable) {
        Object[] result = new Object[baseParams.length + 2];
        System.arraycopy(baseParams, 0, result, 0, baseParams.length);
        result[result.length - 2] = pageable.getPageSize();
        result[result.length - 1] = pageable.getOffset();
        return result;
    }

    private RowMapper<BookingHistory> bookingRowMapper() {
        return new RowMapper<>() {
            @Override
            public BookingHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
                BookingHistory bh = new BookingHistory();
                bh.setId(rs.getLong("id"));
                String resolvedCustomerId = rs.getString("customer_id");
                if (resolvedCustomerId == null || resolvedCustomerId.isBlank()) {
                    resolvedCustomerId = rs.getString("sender_name");
                }
                bh.setCustomerId(resolvedCustomerId);
                bh.setBookingId("BKG-" + rs.getLong("id"));
                bh.setBookingDate(rs.getTimestamp("created_at").toLocalDateTime());
                bh.setReceiverName(rs.getString("receiver_name"));
                bh.setDeliveredAddress(rs.getString("receiver_address"));
                bh.setAmount(rs.getBigDecimal("service_cost"));
                String status = rs.getString("booking_status");
                bh.setStatus(mapStatus(status));
                return bh;
            }
        };
    }

    private BookingHistory.BookingStatus mapStatus(String status) {
        if (status == null) return BookingHistory.BookingStatus.PENDING;
        return switch (status) {
            case "CONFIRMED" -> BookingHistory.BookingStatus.CONFIRMED;
            case "IN_TRANSIT" -> BookingHistory.BookingStatus.IN_TRANSIT;
            case "DELIVERED" -> BookingHistory.BookingStatus.DELIVERED;
            case "CANCELLED" -> BookingHistory.BookingStatus.CANCELLED;
            default -> BookingHistory.BookingStatus.PENDING;
        };
    }

    private PageResponse<BookingHistoryDto> toPageResponse(Page<BookingHistory> page) {
        List<BookingHistoryDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    private BookingHistoryDto toDto(BookingHistory entity) {
        BookingHistoryDto dto = new BookingHistoryDto();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setBookingId(entity.getBookingId());
        dto.setBookingDate(entity.getBookingDate());
        dto.setReceiverName(entity.getReceiverName());
        dto.setDeliveredAddress(entity.getDeliveredAddress());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
