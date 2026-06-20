package com.movieticket.web.dto.catalog;

import com.movieticket.domain.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DiscountCodeResponse(
        UUID id,
        String code,
        DiscountType type,
        BigDecimal value,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        Instant validFrom,
        Instant validUntil,
        Integer maxUsageCount,
        int currentUsageCount,
        boolean active,
        Instant createdAt
) {
}
