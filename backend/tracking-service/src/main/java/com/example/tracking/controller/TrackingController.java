package com.example.tracking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tracking.dto.DeliveryStatusUpdateRequest;
import com.example.tracking.dto.PickupScheduleRequest;
import com.example.tracking.dto.TrackingCreateRequest;
import com.example.tracking.dto.TrackingResponse;
import com.example.tracking.exception.BookingNotFoundException;
import com.example.tracking.service.TrackingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/internal/register")
    public ResponseEntity<TrackingResponse> register(@Valid @RequestBody TrackingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trackingService.registerShipment(request));
    }

    @GetMapping("/officer/shipments")
    public ResponseEntity<List<TrackingResponse>> officerList(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String bookingId) {
        return ResponseEntity.ok(trackingService.listForOfficer(customerId, bookingId));
    }

    @GetMapping("/officer/booking/{bookingId}")
    public ResponseEntity<TrackingResponse> officerLookup(@PathVariable String bookingId) {
        return ResponseEntity.ok(trackingService.findForOfficer(bookingId));
    }

    @PutMapping("/officer/booking/{bookingId}/pickup")
    public ResponseEntity<TrackingResponse> schedulePickup(
            @PathVariable String bookingId,
            @Valid @RequestBody PickupScheduleRequest request) {
        return ResponseEntity.ok(trackingService.schedulePickup(bookingId, request.getPickupDateTime()));
    }

    @PutMapping("/officer/booking/{bookingId}/status")
    public ResponseEntity<TrackingResponse> updateDeliveryStatus(
            @PathVariable String bookingId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        return ResponseEntity.ok(trackingService.updateDeliveryStatus(bookingId, request.getStatus()));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<TrackingResponse> customerTrack(
            @PathVariable String bookingId,
            @RequestParam String customerId) {
        return ResponseEntity.ok(trackingService.trackForCustomer(bookingId, customerId));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError firstError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = firstError != null && firstError.getDefaultMessage() != null
                ? firstError.getDefaultMessage()
                : "Invalid request";
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMalformedRequest(HttpMessageNotReadableException ex) {
        String message = "Invalid request payload";
        Throwable cause = ex.getMostSpecificCause();
        String details = cause != null && cause.getMessage() != null ? cause.getMessage() : "";
        if (details.contains("LocalDateTime")) {
            message = "Pickup date and time format is invalid";
        }
        return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(Map.of("error", message));
    }
}
