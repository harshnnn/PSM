package com.example.tracking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.tracking.service.TrackingService;

@Component
public class TrackingBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TrackingBackfillRunner.class);

    private final TrackingService trackingService;

    public TrackingBackfillRunner(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int inserted = trackingService.backfillFromPaidBookings();
            log.info("Tracking backfill completed. Added {} shipped records.", inserted);
        } catch (Exception ex) {
            log.warn("Tracking backfill skipped: {}", ex.getMessage());
        }
    }
}
