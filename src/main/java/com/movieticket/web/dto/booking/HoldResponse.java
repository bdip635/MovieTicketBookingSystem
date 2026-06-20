package com.movieticket.web.dto.booking;

import com.movieticket.domain.enums.HoldStatus;
import com.movieticket.domain.enums.SeatTier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HoldResponse(
        UUID holdId,
        UUID showId,
        String movieTitle,
        HoldStatus status,
        Instant expiresAt,
        BigDecimal subtotal,
        List<HoldSeatLine> seats
) {
    public record HoldSeatLine(
            UUID showSeatId,
            UUID seatId,
            String rowLabel,
            int seatNumber,
            SeatTier tier,
            BigDecimal unitPrice
    ) {
    }
}
