package com.movieticket.service.pricing;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.domain.enums.DiscountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedAmountDiscountStrategyTest {

    private final FixedAmountDiscountStrategy strategy = new FixedAmountDiscountStrategy();

    @Test
    void appliesFixedDiscount() {
        DiscountCode code = DiscountCode.builder()
                .type(DiscountType.FIXED_AMOUNT)
                .value(new BigDecimal("75.00"))
                .build();

        DiscountResult result = strategy.apply(code, context(new BigDecimal("250.00")));

        assertEquals(new BigDecimal("75.00"), result.discountAmount());
        assertEquals(new BigDecimal("175.00"), result.finalAmount());
    }

    @Test
    void neverDiscountsBelowZero() {
        DiscountCode code = DiscountCode.builder()
                .type(DiscountType.FIXED_AMOUNT)
                .value(new BigDecimal("300.00"))
                .build();

        DiscountResult result = strategy.apply(code, context(new BigDecimal("250.00")));

        assertEquals(new BigDecimal("250.00"), result.discountAmount());
        assertEquals(new BigDecimal("0.00"), result.finalAmount());
    }

    private DiscountContext context(BigDecimal subtotal) {
        return new DiscountContext(subtotal, List.of(), null, null, null);
    }
}
