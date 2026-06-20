package com.movieticket.web.controller;

import com.movieticket.security.SecurityUtils;
import com.movieticket.service.booking.BookingService;
import com.movieticket.web.dto.booking.BookingResponse;
import com.movieticket.web.dto.booking.CancelBookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public List<BookingResponse> listBookings() {
        return bookingService.listBookings(SecurityUtils.currentUserId());
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable UUID bookingId) {
        return bookingService.getBooking(bookingId, SecurityUtils.currentUserId());
    }

    @PostMapping("/{bookingId}/cancel")
    public CancelBookingResponse cancelBooking(@PathVariable UUID bookingId) {
        return bookingService.cancelBooking(bookingId, SecurityUtils.currentUserId());
    }
}
