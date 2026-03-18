package com.example.tracking.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.tracking.dto.BookingSummary;

@Component
public class BookingClient {

    private static final Logger log = LoggerFactory.getLogger(BookingClient.class);

    private final RestTemplate restTemplate;
    private final String bookingBaseUrl;

    public BookingClient(RestTemplate restTemplate,
                         @Value("${booking.service.url:http://booking-service}") String bookingBaseUrl) {
        this.restTemplate = restTemplate;
        this.bookingBaseUrl = bookingBaseUrl;
    }

    public List<BookingSummary> fetchAllBookings() {
        try {
            ResponseEntity<BookingSummary[]> response = restTemplate.getForEntity(
                    bookingBaseUrl + "/api/bookings",
                    BookingSummary[].class);
            BookingSummary[] body = response.getBody();
            if (body == null) {
                return List.of();
            }
            return List.of(body);
        } catch (RestClientException ex) {
            log.warn("Failed to fetch bookings for tracking backfill: {}", ex.getMessage());
            return List.of();
        }
    }
}
