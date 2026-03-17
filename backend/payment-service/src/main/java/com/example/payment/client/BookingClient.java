package com.example.payment.client;

import com.example.payment.dto.BookingPaymentUpdateRequest;
import com.example.payment.dto.BookingSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    public BookingSummary fetchBooking(Long bookingId) {
        try {
            ResponseEntity<BookingSummary> response = restTemplate.getForEntity(
                    bookingBaseUrl + "/api/bookings/" + bookingId,
                    BookingSummary.class);
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("Failed to fetch booking {}: {}", bookingId, ex.getMessage());
            return null;
        }
    }

    public boolean markPaid(Long bookingId, BookingPaymentUpdateRequest request) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    bookingBaseUrl + "/api/bookings/" + bookingId + "/payment",
                    HttpMethod.PUT,
                    new HttpEntity<>(request),
                    Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            log.warn("Failed to update booking {} payment status: {}", bookingId, ex.getMessage());
            return false;
        }
    }
}
