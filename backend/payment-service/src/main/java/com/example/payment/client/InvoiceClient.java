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

import com.example.payment.dto.InvoiceCreateRequest;
import com.example.payment.dto.InvoiceCreateResponse;

@Component
public class InvoiceClient {

    private static final Logger log = LoggerFactory.getLogger(InvoiceClient.class);

    private final RestTemplate restTemplate;
    private final String invoiceBaseUrl;

    public InvoiceClient(RestTemplate restTemplate,
                         @Value("${invoice.service.url:http://invoice-service}") String invoiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.invoiceBaseUrl = invoiceBaseUrl;
    }

    @Nullable
    public InvoiceCreateResponse createInvoice(@NonNull InvoiceCreateRequest request) {
        try {
            InvoiceCreateRequest safeRequest = Objects.requireNonNull(request, "request");
            ResponseEntity<InvoiceCreateResponse> response = restTemplate.postForEntity(
                    invoiceBaseUrl + "/api/invoices",
                    safeRequest,
                    InvoiceCreateResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Invoice creation failed with status {}", response.getStatusCode());
                return null;
            }
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("Invoice creation failed: {}", ex.getMessage());
            return null;
        }
    }
}
