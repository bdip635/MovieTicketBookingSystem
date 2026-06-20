package com.movieticket.web.dto.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RefundPolicyResponse(
        UUID id,
        UUID theaterId,
        String theaterName,
        String name,
        boolean active,
        List<RefundPolicyTierResponse> tiers,
        Instant createdAt
) {
    public record RefundPolicyTierResponse(
            UUID id,
            BigDecimal minHoursBefore,
            BigDecimal refundPercentage,
            int sortOrder
    ) {
    }
}
