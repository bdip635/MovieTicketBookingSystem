package com.movieticket.service.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DiscountContext(
        BigDecimal subtotal,
        List<SeatPricingLine> lines,
        UUID userId,
        UUID showId,
        Instant bookingTime
) {
    public record SeatPricingLine(
            UUID seatId,
            String rowLabel,
            int seatNumber,
            BigDecimal unitPrice
    ) {
    }
}
