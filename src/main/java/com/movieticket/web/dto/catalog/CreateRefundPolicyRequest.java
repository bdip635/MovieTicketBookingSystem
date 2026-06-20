package com.movieticket.web.dto.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateRefundPolicyRequest(
        @NotNull UUID theaterId,
        @NotBlank @Size(max = 255) String name,
        boolean active,
        @NotEmpty List<@Valid RefundPolicyTierRequest> tiers
) {
    public record RefundPolicyTierRequest(
            @NotNull @DecimalMin("0") BigDecimal minHoursBefore,
            @NotNull @DecimalMin("0") BigDecimal refundPercentage,
            int sortOrder
    ) {
    }
}
