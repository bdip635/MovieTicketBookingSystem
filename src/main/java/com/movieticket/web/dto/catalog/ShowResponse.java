package com.movieticket.web.dto.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShowResponse(
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
        Instant createdAt
) {
}
