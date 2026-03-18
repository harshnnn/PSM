package com.example.tracking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tracking.dto.TrackingCreateRequest;
import com.example.tracking.dto.TrackingResponse;
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

    @GetMapping("/{bookingId}")
    public ResponseEntity<TrackingResponse> customerTrack(
            @PathVariable String bookingId,
            @RequestParam String customerId) {
        return ResponseEntity.ok(trackingService.trackForCustomer(bookingId, customerId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
