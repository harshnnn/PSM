package com.example.booking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.PaymentUpdateRequest;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingHistoryRecord;
import com.example.booking.repository.BookingHistoryRepository;
import com.example.booking.repository.BookingRepository;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingHistoryRepository bookingHistoryRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
    }

    @Override
    public BookingResponse create(BookingRequest request) {
        if (isSameContact(request.getSenderContact(), request.getReceiverContact())) {
            throw new IllegalArgumentException("Sender and receiver contact must be different");
        }
        validateParcelWeight(request);

        Booking booking = new Booking();
        booking.setSenderName(request.getSenderName());
        booking.setSenderAddress(request.getSenderAddress());
        booking.setSenderContact(request.getSenderContact());
        booking.setCustomerId(request.getCustomerId());
        booking.setReceiverName(request.getReceiverName());
        booking.setReceiverAddress(request.getReceiverAddress());
        booking.setReceiverPinCode(request.getReceiverPinCode());
        booking.setReceiverContact(request.getReceiverContact());
        booking.setParcelSize(request.getParcelSize());
        booking.setWeightKg(request.getWeightKg());
        booking.setContentsDescription(request.getContentsDescription());
        booking.setDeliverySpeed(request.getDeliverySpeed());
        booking.setPackagingPreference(request.getPackagingPreference());
        booking.setPreferredPickup(request.getPreferredPickup());
        booking.setServiceCost(request.getServiceCost());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setInsuranceSelected(request.isInsuranceSelected());
        booking.setTrackingEnabled(request.isTrackingEnabled());
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    private void validateParcelWeight(BookingRequest request) {
        if (request.getParcelSize() == null || request.getWeightKg() == null) {
            return;
        }

        BigDecimal maxAllowed = switch (request.getParcelSize()) {
            case SMALL -> BigDecimal.valueOf(5);
            case MEDIUM -> BigDecimal.valueOf(20);
            case LARGE -> BigDecimal.valueOf(50);
            case CUSTOM -> BigDecimal.valueOf(999);
        };

        if (request.getWeightKg().compareTo(maxAllowed) > 0) {
            throw new IllegalArgumentException(
                "Weight exceeds allowed limit for " + request.getParcelSize() + " parcel (max " + maxAllowed + " kg)"
            );
        }
    }

    private boolean isSameContact(String senderContact, String receiverContact) {
        String sender = normalizeContact(senderContact);
        String receiver = normalizeContact(receiverContact);
        return !sender.isBlank() && sender.equals(receiver);
    }

    private String normalizeContact(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse get(long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> list() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listUnpaidForCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return List.of();
        }

        return bookingRepository.findByCustomerIdAndPaymentStatus(customerId, Booking.PaymentStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BookingResponse updatePaymentStatus(long id, PaymentUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID
                && request.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            throw new IllegalStateException("Booking is already marked as paid");
        }

        if (request.getPaymentStatus() != null) {
            booking.setPaymentStatus(request.getPaymentStatus());
        }

        if (request.getBookingStatus() != null) {
            booking.setBookingStatus(request.getBookingStatus());
        } else if (request.getPaymentStatus() == Booking.PaymentStatus.PAID
                && booking.getBookingStatus() == Booking.BookingStatus.PENDING) {
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        }

        Booking saved = bookingRepository.save(booking);
        persistHistoryIfPaid(saved);
        return toResponse(Objects.requireNonNull(saved, "saved booking"));
    }

    private BookingResponse toResponse(Booking booking) {
        Booking safe = Objects.requireNonNull(booking, "booking");
        BookingResponse dto = new BookingResponse();
        dto.setId(safe.getId());
        dto.setCustomerId(safe.getCustomerId());
        dto.setSenderName(safe.getSenderName());
        dto.setReceiverName(safe.getReceiverName());
        dto.setReceiverAddress(safe.getReceiverAddress());
        dto.setReceiverPinCode(safe.getReceiverPinCode());
        dto.setReceiverContact(safe.getReceiverContact());
        dto.setParcelSize(safe.getParcelSize());
        dto.setWeightKg(safe.getWeightKg());
        dto.setDeliverySpeed(safe.getDeliverySpeed());
        dto.setPackagingPreference(safe.getPackagingPreference());
        dto.setContentsDescription(safe.getContentsDescription());
        dto.setPaymentStatus(safe.getPaymentStatus());
        dto.setBookingStatus(safe.getBookingStatus());
        dto.setServiceCost(safe.getServiceCost());
        dto.setPreferredPickup(safe.getPreferredPickup());
        dto.setCreatedAt(safe.getCreatedAt());
        return dto;
    }

    private void persistHistoryIfPaid(Booking booking) {
        Booking safe = Objects.requireNonNull(booking, "booking");
        if (safe.getPaymentStatus() != Booking.PaymentStatus.PAID) {
            return;
        }

        String bookingKey = "BKG-" + safe.getId();
        if (bookingHistoryRepository.existsByBookingId(bookingKey)) {
            return;
        }

        BookingHistoryRecord record = new BookingHistoryRecord();
        String resolvedCustomerId = safe.getCustomerId();
        if (resolvedCustomerId == null || resolvedCustomerId.isBlank()) {
            resolvedCustomerId = safe.getSenderName();
        }
        record.setCustomerId(resolvedCustomerId);
        record.setBookingId(bookingKey);
        record.setBookingDate(safe.getCreatedAt());
        record.setReceiverName(safe.getReceiverName());
        record.setDeliveredAddress(safe.getReceiverAddress());
        record.setAmount(safe.getServiceCost());
        record.setStatus(mapStatus(safe.getBookingStatus()));
        bookingHistoryRepository.save(record);
    }

    private BookingHistoryRecord.BookingStatus mapStatus(Booking.BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> BookingHistoryRecord.BookingStatus.CONFIRMED;
            case CANCELLED -> BookingHistoryRecord.BookingStatus.CANCELLED;
            default -> BookingHistoryRecord.BookingStatus.PENDING;
        };
    }
}
