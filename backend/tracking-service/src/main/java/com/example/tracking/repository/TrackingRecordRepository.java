package com.example.tracking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tracking.entity.TrackingRecord;

public interface TrackingRecordRepository extends JpaRepository<TrackingRecord, Long> {
    Optional<TrackingRecord> findByOriginalBookingId(Long originalBookingId);
    Optional<TrackingRecord> findByTrackingNumber(String trackingNumber);
    boolean existsByTrackingNumber(String trackingNumber);
    List<TrackingRecord> findAllByOrderByShippedAtDesc();
}
