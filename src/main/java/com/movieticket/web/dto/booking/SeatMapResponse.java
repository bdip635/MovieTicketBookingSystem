package com.movieticket.web.dto.booking;

import com.movieticket.domain.enums.SeatStatus;
import com.movieticket.domain.enums.SeatTier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeatMapResponse(
        UUID showId,
        String movieTitle,
        Instant showStartTime,
        List<SeatMapEntry> seats,
        int availableCount,
        int heldCount,
        int bookedCount
) {
    public record SeatMapEntry(
            UUID showSeatId,
            UUID seatId,
            String rowLabel,
            int seatNumber,
            SeatTier tier,
            SeatStatus status,
            Instant holdExpiresAt
    ) {
    }
}
