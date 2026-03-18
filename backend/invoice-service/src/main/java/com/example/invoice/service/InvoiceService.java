package com.example.invoice.service;

import java.util.List;

import com.example.invoice.dto.InvoiceRequest;
import com.example.invoice.dto.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse create(InvoiceRequest request);
    InvoiceResponse get(Long id);
    List<InvoiceResponse> list(String customerId);
}
