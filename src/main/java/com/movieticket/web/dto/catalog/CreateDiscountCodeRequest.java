package com.movieticket.web.dto.catalog;

import com.movieticket.domain.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateDiscountCodeRequest(
        @NotBlank @Size(max = 64) String code,
        @NotNull DiscountType type,
        @NotNull @DecimalMin("0.01") BigDecimal value,
        @DecimalMin("0.01") BigDecimal maxDiscountAmount,
        @DecimalMin("0.01") BigDecimal minOrderAmount,
        @NotNull Instant validFrom,
        @NotNull Instant validUntil,
        Integer maxUsageCount,
        boolean active
) {
}
