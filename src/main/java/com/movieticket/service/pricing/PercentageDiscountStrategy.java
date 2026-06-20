package com.movieticket.service.pricing;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.domain.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean supports(DiscountType type) {
        return type == DiscountType.PERCENTAGE;
    }

    @Override
    public void validate(DiscountCode code, DiscountContext context) {
        if (code.getValue().compareTo(BigDecimal.ZERO) <= 0 || code.getValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount must be between 0 and 100");
        }
    }

    @Override
    public DiscountResult apply(DiscountCode code, DiscountContext context) {
        BigDecimal discount = context.subtotal()
                .multiply(code.getValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (code.getMaxDiscountAmount() != null) {
            discount = discount.min(code.getMaxDiscountAmount());
        }

        return toResult(context.subtotal(), discount, code.getValue() + "% off");
    }

    static DiscountResult toResult(BigDecimal subtotal, BigDecimal discount, String description) {
        discount = discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new DiscountResult(discount, finalAmount, description);
    }
}
