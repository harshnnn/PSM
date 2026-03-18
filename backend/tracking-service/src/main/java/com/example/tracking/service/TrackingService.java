package com.example.tracking.service;

import java.util.List;
import java.time.LocalDateTime;

import com.example.tracking.dto.TrackingCreateRequest;
import com.example.tracking.dto.TrackingResponse;

public interface TrackingService {
    TrackingResponse registerShipment(TrackingCreateRequest request);
    TrackingResponse trackForCustomer(String bookingId, String customerId);
    List<TrackingResponse> listForOfficer(String customerId, String bookingId);
    TrackingResponse findForOfficer(String bookingId);
    TrackingResponse schedulePickup(String bookingId, LocalDateTime pickupDateTime);
    TrackingResponse updateDeliveryStatus(String bookingId, String status);
    int backfillFromPaidBookings();
}
