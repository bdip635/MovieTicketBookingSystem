package com.movieticket.service.pricing;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.domain.enums.DiscountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentageDiscountStrategyTest {

    private final PercentageDiscountStrategy strategy = new PercentageDiscountStrategy();

    @Test
    void appliesPercentageWithoutCap() {
        DiscountCode code = DiscountCode.builder()
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("20"))
                .build();

        DiscountResult result = strategy.apply(code, context(new BigDecimal("500.00")));

        assertEquals(new BigDecimal("100.00"), result.discountAmount());
        assertEquals(new BigDecimal("400.00"), result.finalAmount());
    }

    @Test
    void capsPercentageDiscount() {
        DiscountCode code = DiscountCode.builder()
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("20"))
                .maxDiscountAmount(new BigDecimal("50.00"))
                .build();

        DiscountResult result = strategy.apply(code, context(new BigDecimal("500.00")));

        assertEquals(new BigDecimal("50.00"), result.discountAmount());
        assertEquals(new BigDecimal("450.00"), result.finalAmount());
    }

    private DiscountContext context(BigDecimal subtotal) {
        return new DiscountContext(subtotal, List.of(), null, null, null);
    }
}
