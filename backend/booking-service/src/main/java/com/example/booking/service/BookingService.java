package com.example.booking.service;

import java.util.List;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.BookingResponse;

public interface BookingService {

    BookingResponse create(BookingRequest request);

    BookingResponse get(long id);

    List<BookingResponse> list();
}
