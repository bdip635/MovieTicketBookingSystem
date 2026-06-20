package com.movieticket.web.dto.catalog;

import com.movieticket.domain.enums.SeatTier;

import java.time.Instant;
import java.util.UUID;

public record SeatResponse(
        UUID id,
        UUID screenId,
        String rowLabel,
        int seatNumber,
        SeatTier tier,
        Instant createdAt
) {
}
