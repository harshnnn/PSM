package com.example.payment.client;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.payment.dto.TrackingCreateRequest;
import com.example.payment.dto.TrackingCreateResponse;

@Component
public class TrackingClient {

    private static final Logger log = LoggerFactory.getLogger(TrackingClient.class);

    private final RestTemplate restTemplate;
    private final String trackingBaseUrl;

    public TrackingClient(
            RestTemplate restTemplate,
            @Value("${tracking.service.url:http://tracking-service}") String trackingBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.trackingBaseUrl = trackingBaseUrl;
    }

    @Nullable
    public TrackingCreateResponse registerShipment(@NonNull TrackingCreateRequest request) {
        try {
            TrackingCreateRequest safeRequest = Objects.requireNonNull(request, "request");
            ResponseEntity<TrackingCreateResponse> response = restTemplate.postForEntity(
                    trackingBaseUrl + "/api/tracking/internal/register",
                    safeRequest,
                    TrackingCreateResponse.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Tracking registration failed with status {}", response.getStatusCode());
                return null;
            }
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("Tracking registration failed: {}", ex.getMessage());
            return null;
        }
    }
}
