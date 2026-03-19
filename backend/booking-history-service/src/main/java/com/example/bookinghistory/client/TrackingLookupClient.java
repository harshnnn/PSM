package com.example.bookinghistory.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.bookinghistory.dto.TrackingLookupResponse;

@Component
public class TrackingLookupClient {

    private static final Logger log = LoggerFactory.getLogger(TrackingLookupClient.class);

    private final RestTemplate restTemplate;
    private final String trackingBaseUrl;

    public TrackingLookupClient(
            RestTemplate restTemplate,
            @Value("${tracking.service.url:http://tracking-service}") String trackingBaseUrl) {
        this.restTemplate = restTemplate;
        this.trackingBaseUrl = trackingBaseUrl;
    }

    public TrackingLookupResponse findTrackingDetails(Long originalBookingId) {
        if (originalBookingId == null) {
            return null;
        }

        try {
            ResponseEntity<TrackingLookupResponse> response = restTemplate.getForEntity(
                    trackingBaseUrl + "/api/tracking/officer/booking/" + originalBookingId,
                    TrackingLookupResponse.class);
            TrackingLookupResponse body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                return null;
            }
            return body;
        } catch (RestClientException ex) {
            log.debug("Tracking details lookup failed for booking {}: {}", originalBookingId, ex.getMessage());
            return null;
        }
    }

    public String findTrackingBookingId(Long originalBookingId) {
        TrackingLookupResponse details = findTrackingDetails(originalBookingId);
        return details != null ? details.getBookingId() : null;
    }
}