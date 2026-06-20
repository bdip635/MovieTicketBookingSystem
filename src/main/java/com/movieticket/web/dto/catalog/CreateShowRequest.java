package com.movieticket.web.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateShowRequest(
        @NotNull UUID movieId,
        @NotNull UUID screenId,
        @NotNull Instant startTime,
        @NotNull @DecimalMin("0.01") BigDecimal regularBasePrice,
        @NotNull @DecimalMin("0.01") BigDecimal premiumBasePrice,
        @DecimalMin("1.00") BigDecimal weekendMultiplier
) {
}
