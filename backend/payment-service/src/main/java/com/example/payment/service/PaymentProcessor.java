package com.example.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.client.BookingClient;
import com.example.payment.client.InvoiceClient;
import com.example.payment.client.TrackingClient;
import com.example.payment.dto.BookingPaymentUpdateRequest;
import com.example.payment.dto.BookingSummary;
import com.example.payment.dto.InvoiceCreateRequest;
import com.example.payment.dto.InvoiceCreateResponse;
import com.example.payment.dto.PaymentBillResponse;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.TrackingCreateRequest;
import com.example.payment.dto.TrackingCreateResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.Payment.PaymentStatus;
import com.example.payment.repository.PaymentRepository;

@Service
public class PaymentProcessor {

    private final PaymentRepository repository;
    private final BookingClient bookingClient;
    private final InvoiceClient invoiceClient;
    private final TrackingClient trackingClient;

    public PaymentProcessor(PaymentRepository repository, BookingClient bookingClient, InvoiceClient invoiceClient, TrackingClient trackingClient) {
        this.repository = repository;
        this.bookingClient = bookingClient;
        this.invoiceClient = invoiceClient;
        this.trackingClient = trackingClient;
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
            throw new IllegalStateException("Payment already completed for this booking.");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking is cancelled and cannot be paid.");
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

        boolean updated = bookingClient.markPaid(booking.getId(), new BookingPaymentUpdateRequest("PAID", "CONFIRMED"));
        if (!updated) {
            throw new IllegalStateException("Payment captured but booking could not be marked as paid. Please retry.");
        }

        InvoiceCreateResponse invoice = createInvoice(booking, saved.getTransactionRef(), request.getPaymentMode(), expectedAmount);
        TrackingCreateResponse tracking = registerTracking(booking, expectedAmount);

        return new PaymentResponse(
                saved.getBookingId(),
            invoice != null ? invoice.getId() : null,
            invoice != null ? invoice.getInvoiceNumber() : null,
            tracking != null ? tracking.getBookingId() : null,
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

    private InvoiceCreateResponse createInvoice(BookingSummary booking, String transactionRef, Payment.PaymentMode paymentMode, BigDecimal amount) {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setBookingId(booking.getId());
        req.setCustomerId(resolveCustomerId(booking));
        req.setReceiverName(defaultString(booking.getReceiverName(), "Receiver"));
        req.setReceiverAddress(defaultString(booking.getReceiverAddress(), "Unknown address"));
        req.setReceiverPin(defaultString(booking.getReceiverPinCode(), "000000"));
        req.setReceiverMobile(defaultString(booking.getReceiverContact(), "0000000000"));

        BigDecimal grams = null;
        if (booking.getWeightKg() != null) {
            grams = booking.getWeightKg().multiply(BigDecimal.valueOf(1000));
        }
        if (grams == null || grams.compareTo(BigDecimal.valueOf(1)) < 0) {
            grams = BigDecimal.valueOf(100); // sensible default to satisfy validation
        }
        req.setParcelWeightGrams(grams);

        req.setContentsDescription(defaultString(booking.getContentsDescription(), "Parcel"));
        req.setParcelDeliveryType(defaultString(booking.getDeliverySpeed(), "STANDARD"));
        req.setParcelPackingPreference(defaultString(booking.getPackagingPreference(), "STANDARD"));

        req.setParcelPickupTime(booking.getPreferredPickup() != null ? booking.getPreferredPickup() : LocalDateTime.now());
        req.setParcelDropoffTime(null);
        req.setParcelServiceCost(amount);
        req.setPaymentTime(LocalDateTime.now());
        req.setPaymentMode(paymentMode.name());
        req.setTransactionRef(transactionRef);
        return invoiceClient.createInvoice(req);
    }

    private TrackingCreateResponse registerTracking(BookingSummary booking, BigDecimal amount) {
        TrackingCreateRequest request = new TrackingCreateRequest();
        request.setOriginalBookingId(booking.getId());
        request.setCustomerId(resolveCustomerId(booking));
        request.setReceiverName(defaultString(booking.getReceiverName(), "Receiver"));
        request.setAmount(amount);
        return trackingClient.registerShipment(request);
    }

    private String defaultString(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String generateRef() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

}
