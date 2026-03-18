package com.example.invoice.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.invoice.dto.InvoiceRequest;
import com.example.invoice.dto.InvoiceResponse;
import com.example.invoice.entity.Invoice;
import com.example.invoice.repository.InvoiceRepository;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;
    private final SecureRandom random = new SecureRandom();

    public InvoiceServiceImpl(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public InvoiceResponse create(InvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber(request.getBookingId()));
        invoice.setBookingId(request.getBookingId());
        invoice.setCustomerId(request.getCustomerId());
        invoice.setReceiverName(request.getReceiverName());
        invoice.setReceiverAddress(request.getReceiverAddress());
        invoice.setReceiverPin(request.getReceiverPin());
        invoice.setReceiverMobile(request.getReceiverMobile());
        invoice.setParcelWeightGrams(request.getParcelWeightGrams());
        invoice.setContentsDescription(request.getContentsDescription());
        invoice.setParcelDeliveryType(request.getParcelDeliveryType());
        invoice.setParcelPackingPreference(request.getParcelPackingPreference());
        invoice.setParcelPickupTime(request.getParcelPickupTime());
        invoice.setParcelDropoffTime(request.getParcelDropoffTime());
        invoice.setParcelServiceCost(request.getParcelServiceCost());
        invoice.setPaymentTime(request.getPaymentTime());
        invoice.setPaymentMode(request.getPaymentMode());
        invoice.setTransactionRef(request.getTransactionRef());

        Invoice saved = repository.save(invoice);
        return toResponse(Objects.requireNonNull(saved, "saved invoice"));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> list(String customerId) {
        List<Invoice> invoices;
        if (customerId != null && !customerId.isBlank()) {
            invoices = repository.findByCustomerIdOrderByCreatedAtDesc(customerId.trim());
        } else {
            invoices = repository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        }
        return invoices.stream().map(this::toResponse).toList();
    }

    private String generateInvoiceNumber(Long bookingId) {
        String base = "INV-" + (bookingId != null ? bookingId : "NA");
        String suffix = random.ints(6, 0, 36)
                .mapToObj(this::toAlphanumeric)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        String candidate = base + "-" + suffix;
        if (repository.existsByInvoiceNumber(candidate)) {
            return generateInvoiceNumber(bookingId);
        }
        return candidate;
    }

    private String toAlphanumeric(int value) {
        char c = Character.forDigit(value, 36);
        return String.valueOf(c).toUpperCase(Locale.ROOT);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        Invoice safe = Objects.requireNonNull(invoice, "invoice");
        InvoiceResponse dto = new InvoiceResponse();
        dto.setId(safe.getId());
        dto.setInvoiceNumber(safe.getInvoiceNumber());
        dto.setBookingId(safe.getBookingId());
        dto.setCustomerId(safe.getCustomerId());
        dto.setReceiverName(safe.getReceiverName());
        dto.setReceiverAddress(safe.getReceiverAddress());
        dto.setReceiverPin(safe.getReceiverPin());
        dto.setReceiverMobile(safe.getReceiverMobile());
        dto.setParcelWeightGrams(safe.getParcelWeightGrams());
        dto.setContentsDescription(safe.getContentsDescription());
        dto.setParcelDeliveryType(safe.getParcelDeliveryType());
        dto.setParcelPackingPreference(safe.getParcelPackingPreference());
        dto.setParcelPickupTime(safe.getParcelPickupTime());
        dto.setParcelDropoffTime(safe.getParcelDropoffTime());
        dto.setParcelServiceCost(safe.getParcelServiceCost());
        dto.setPaymentTime(safe.getPaymentTime());
        dto.setPaymentMode(safe.getPaymentMode());
        dto.setTransactionRef(safe.getTransactionRef());
        dto.setCreatedAt(safe.getCreatedAt());
        return dto;
    }
}
