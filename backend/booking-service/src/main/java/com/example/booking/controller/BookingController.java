package com.example.booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.PaymentUpdateRequest;
import com.example.booking.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private void rejectOfficerForCustomerActions(String userRole) {
        if ("OFFICER".equalsIgnoreCase(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Officer is not allowed to perform customer booking/payment actions");
        }
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request,
                                                  @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        rejectOfficerForCustomerActions(userRole);
        BookingResponse response = bookingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable long id) {
        return ResponseEntity.ok(bookingService.get(id));
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<BookingResponse>> listUnpaid(@RequestParam String customerId,
                                                            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        rejectOfficerForCustomerActions(userRole);
        return ResponseEntity.ok(bookingService.listUnpaidForCustomer(customerId));
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<BookingResponse> updatePayment(@PathVariable long id,
                                                         @Valid @RequestBody PaymentUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updatePaymentStatus(id, request));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> list() {
        return ResponseEntity.ok(bookingService.list());
    }
}
