package com.movieticket.service.pricing;

import java.math.BigDecimal;

public record DiscountResult(
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String description
) {
}
