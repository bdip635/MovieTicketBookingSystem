package com.movieticket.web.dto.booking;

import com.movieticket.domain.enums.SeatTier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerShowResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        UUID screenId,
        String screenName,
        UUID theaterId,
        String theaterName,
        UUID cityId,
        String cityName,
        Instant startTime,
        BigDecimal regularBasePrice,
        BigDecimal premiumBasePrice,
        BigDecimal weekendMultiplier,
        int totalSeats,
        int availableSeats,
        int heldSeats,
        int bookedSeats,
        Instant createdAt
) {
}
