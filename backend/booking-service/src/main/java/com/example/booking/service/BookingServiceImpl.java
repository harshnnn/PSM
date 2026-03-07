package com.example.booking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingResponse create(BookingRequest request) {
        Booking booking = new Booking();
        booking.setSenderName(request.getSenderName());
        booking.setSenderAddress(request.getSenderAddress());
        booking.setSenderContact(request.getSenderContact());
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

    @Override
    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
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

    private BookingResponse toResponse(Booking booking) {
        BookingResponse dto = new BookingResponse();
        dto.setId(booking.getId());
        dto.setSenderName(booking.getSenderName());
        dto.setReceiverName(booking.getReceiverName());
        dto.setReceiverPinCode(booking.getReceiverPinCode());
        dto.setParcelSize(booking.getParcelSize());
        dto.setWeightKg(booking.getWeightKg());
        dto.setDeliverySpeed(booking.getDeliverySpeed());
        dto.setPackagingPreference(booking.getPackagingPreference());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setServiceCost(booking.getServiceCost());
        dto.setPreferredPickup(booking.getPreferredPickup());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }
}
