package com.example.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.client.BookingClient;
import com.example.payment.dto.BookingPaymentUpdateRequest;
import com.example.payment.dto.BookingSummary;
import com.example.payment.dto.PaymentBillResponse;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.Payment.PaymentMode;
import com.example.payment.entity.Payment.PaymentStatus;
import com.example.payment.repository.PaymentRepository;

@Service
public class PaymentProcessor {

    private final PaymentRepository repository;
    private final BookingClient bookingClient;

    public PaymentProcessor(PaymentRepository repository, BookingClient bookingClient) {
        this.repository = repository;
        this.bookingClient = bookingClient;
    }

    @Transactional(readOnly = true)
    public PaymentBillResponse fetchBill(Long bookingId) {
        BookingSummary booking = bookingClient.fetchBooking(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found for ID " + bookingId);
        }
        PaymentBillResponse response = new PaymentBillResponse();
        response.setBookingId(booking.getId());
        response.setCustomerId(booking.getCustomerId());
        response.setAmount(booking.getServiceCost());
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setBookingStatus(booking.getBookingStatus());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }

    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        BookingSummary booking = bookingClient.fetchBooking(request.getBookingId());
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found for ID " + request.getBookingId());
        }

        BigDecimal expectedAmount = booking.getServiceCost();
        if (expectedAmount == null) {
            throw new IllegalStateException("Booking cost not available");
        }
        if (request.getAmount().compareTo(expectedAmount) != 0) {
            throw new IllegalArgumentException("Bill amount mismatch. Expected " + expectedAmount);
        }

        String paymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "PENDING";

        if ("PAID".equalsIgnoreCase(paymentStatus)) {
            return new PaymentResponse(
                    booking.getId(),
                    booking.getCustomerId(),
                    expectedAmount,
                    request.getPaymentMode(),
                    PaymentStatus.SUCCESS,
                    lastTransactionRef(booking.getId()),
                    "Payment already completed for this booking.");
        }

        validateExpiry(request.getExpiry());

        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setCustomerId(resolveCustomerId(booking));
        payment.setAmount(expectedAmount);
        payment.setPaymentMode(request.getPaymentMode());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef(generateRef());
        payment.setCardLast4(request.getCardNumber().substring(request.getCardNumber().length() - 4));
        Payment saved = repository.save(payment);

        bookingClient.markPaid(booking.getId(), new BookingPaymentUpdateRequest("PAID", "CONFIRMED"));

        return new PaymentResponse(
                saved.getBookingId(),
                saved.getCustomerId(),
                saved.getAmount(),
                saved.getPaymentMode(),
                saved.getStatus(),
                saved.getTransactionRef(),
                "Payment Successful");
    }

    private String resolveCustomerId(BookingSummary booking) {
        if (booking.getCustomerId() != null && !booking.getCustomerId().isBlank()) {
            return booking.getCustomerId();
        }
        return "customer";
    }

    private void validateExpiry(String expiry) {
        YearMonth provided = YearMonth.parse("20" + expiry.substring(3) + "-" + expiry.substring(0, 2));
        YearMonth current = YearMonth.from(LocalDate.now());
        if (provided.isBefore(current)) {
            throw new IllegalArgumentException("Card is expired");
        }
    }

    private String generateRef() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String lastTransactionRef(Long bookingId) {
        return repository.findTopByBookingIdOrderByCreatedAtDesc(bookingId)
                .map(Payment::getTransactionRef)
                .orElse("TXN-PAID");
    }
}
