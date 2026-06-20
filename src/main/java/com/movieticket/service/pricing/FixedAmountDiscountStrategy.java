package com.movieticket.service.pricing;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.domain.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean supports(DiscountType type) {
        return type == DiscountType.FIXED_AMOUNT;
    }

    @Override
    public void validate(DiscountCode code, DiscountContext context) {
        if (code.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fixed discount amount must be positive");
        }
    }

    @Override
    public DiscountResult apply(DiscountCode code, DiscountContext context) {
        BigDecimal discount = code.getValue().min(context.subtotal());
        return PercentageDiscountStrategy.toResult(context.subtotal(), discount, "Flat " + code.getValue() + " off");
    }
}
