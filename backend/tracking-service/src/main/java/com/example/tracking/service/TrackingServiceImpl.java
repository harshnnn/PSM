package com.example.tracking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tracking.client.BookingClient;
import com.example.tracking.dto.BookingSummary;
import com.example.tracking.dto.TrackingCreateRequest;
import com.example.tracking.dto.TrackingResponse;
import com.example.tracking.entity.TrackingRecord;
import com.example.tracking.entity.TrackingRecord.TrackingStatus;
import com.example.tracking.exception.BookingNotFoundException;
import com.example.tracking.repository.TrackingRecordRepository;

@Service
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private static final Set<TrackingStatus> OFFICER_ALLOWED_STATUSES = Set.of(
            TrackingStatus.PICKED_UP,
            TrackingStatus.IN_TRANSIT,
            TrackingStatus.DELIVERED,
            TrackingStatus.RETURNED);

    private final TrackingRecordRepository repository;
    private final BookingClient bookingClient;

    public TrackingServiceImpl(TrackingRecordRepository repository, BookingClient bookingClient) {
        this.repository = repository;
        this.bookingClient = bookingClient;
    }

    @Override
    public TrackingResponse registerShipment(TrackingCreateRequest request) {
        TrackingRecord existing = repository.findByOriginalBookingId(request.getOriginalBookingId()).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        TrackingRecord record = new TrackingRecord();
        record.setOriginalBookingId(request.getOriginalBookingId());
        record.setTrackingNumber(generateBookingId());
        record.setCustomerId(request.getCustomerId().trim());
        record.setReceiverName(request.getReceiverName());
        record.setAmount(request.getAmount());
        record.setStatus(TrackingStatus.SHIPPED);
        record.setShippedAt(LocalDateTime.now());

        TrackingRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse trackForCustomer(String bookingId, String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        TrackingRecord record = repository.findByTrackingNumber(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (!record.getCustomerId().equalsIgnoreCase(customerId.trim())) {
            throw new BookingNotFoundException("Booking not found");
        }

        return toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingResponse> listForOfficer(String customerId, String bookingId) {
        return repository.findAllByOrderByShippedAtDesc()
                .stream()
                .filter(record -> matchCustomer(record, customerId))
                .filter(record -> matchBooking(record, bookingId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse findForOfficer(String bookingId) {
        return toResponse(findByBookingIdentifier(bookingId));
    }

    @Override
    public TrackingResponse schedulePickup(String bookingId, LocalDateTime pickupDateTime) {
        TrackingRecord record = findByBookingIdentifier(bookingId);
        if (pickupDateTime == null) {
            throw new IllegalArgumentException("Pickup date and time are required");
        }
        if (pickupDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup date and time cannot be in the past");
        }
        record.setPickupScheduledAt(pickupDateTime);
        TrackingRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Override
    public TrackingResponse updateDeliveryStatus(String bookingId, String status) {
        TrackingRecord record = findByBookingIdentifier(bookingId);
        TrackingStatus parsedStatus = parseOfficerStatus(status);
        record.setStatus(parsedStatus);
        TrackingRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Override
    public int backfillFromPaidBookings() {
        List<BookingSummary> bookings = bookingClient.fetchAllBookings();
        int inserted = 0;
        for (BookingSummary booking : bookings) {
            if (booking.getId() == null) {
                continue;
            }
            if (!"PAID".equalsIgnoreCase(booking.getPaymentStatus())) {
                continue;
            }
            if (repository.findByOriginalBookingId(booking.getId()).isPresent()) {
                continue;
            }

            TrackingRecord record = new TrackingRecord();
            record.setOriginalBookingId(booking.getId());
            record.setTrackingNumber(generateBookingId());
            record.setCustomerId(defaultString(booking.getCustomerId(), "customer"));
            record.setReceiverName(defaultString(booking.getReceiverName(), "Receiver"));
            record.setAmount(booking.getServiceCost() != null ? booking.getServiceCost() : BigDecimal.ZERO);
            record.setStatus(TrackingStatus.SHIPPED);
            record.setShippedAt(booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now());
            repository.save(record);
            inserted++;
        }
        return inserted;
    }

    private boolean matchCustomer(TrackingRecord record, String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return true;
        }
        return record.getCustomerId().equalsIgnoreCase(customerId.trim());
    }

    private boolean matchBooking(TrackingRecord record, String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            return true;
        }
        String normalized = bookingId.trim();
        return normalized.equals(record.getTrackingNumber())
                || normalized.equals(String.valueOf(record.getOriginalBookingId()));
    }

    private TrackingResponse toResponse(TrackingRecord record) {
        TrackingResponse response = new TrackingResponse();
        response.setBookingId(record.getTrackingNumber());
        response.setOriginalBookingId(record.getOriginalBookingId());
        response.setCustomerId(record.getCustomerId());
        response.setReceiverName(record.getReceiverName());
        response.setAmount(record.getAmount());
        response.setTrackingStatus(record.getStatus().name());
        response.setShippedAt(record.getShippedAt());
        response.setPickupScheduledAt(record.getPickupScheduledAt());
        response.setLastUpdatedAt(record.getUpdatedAt());
        return response;
    }

    private TrackingRecord findByBookingIdentifier(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        String normalized = bookingId.trim();

        TrackingRecord byTracking = repository.findByTrackingNumber(normalized).orElse(null);
        if (byTracking != null) {
            return byTracking;
        }

        try {
            return repository.findByOriginalBookingId(Long.parseLong(normalized))
                    .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        } catch (NumberFormatException ex) {
            throw new BookingNotFoundException("Booking not found");
        }
    }

    private TrackingStatus parseOfficerStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Delivery status is required");
        }
        try {
            TrackingStatus parsed = TrackingStatus.valueOf(status.trim().toUpperCase());
            if (!OFFICER_ALLOWED_STATUSES.contains(parsed)) {
                throw new IllegalArgumentException("Unsupported delivery status");
            }
            return parsed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported delivery status");
        }
    }

    private String generateBookingId() {
        String candidate = String.format("%012d", ThreadLocalRandom.current().nextLong(0, 1_000_000_000_000L));
        if (repository.existsByTrackingNumber(candidate)) {
            return generateBookingId();
        }
        return candidate;
    }

    private String defaultString(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
