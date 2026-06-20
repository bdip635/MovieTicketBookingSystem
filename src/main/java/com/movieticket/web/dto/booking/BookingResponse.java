package com.movieticket.web.dto.booking;

import com.movieticket.domain.enums.BookingStatus;
import com.movieticket.domain.enums.SeatTier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        String confirmationCode,
        BookingStatus status,
        UUID showId,
        String movieTitle,
        Instant showStartTime,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String discountCode,
        String discountDescription,
        String paymentTransactionId,
        Instant createdAt,
        List<BookingSeatLine> seats
) {
    public record BookingSeatLine(
            UUID showSeatId,
            UUID seatId,
            String rowLabel,
            int seatNumber,
            SeatTier tier,
            BigDecimal unitPrice
    ) {
    }
}
