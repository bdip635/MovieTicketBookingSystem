package com.movieticket.service.pricing;

import com.movieticket.domain.entity.DiscountCode;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;
    private final List<DiscountStrategy> strategies;

    public DiscountResult applyDiscount(String codeValue, DiscountContext context) {
        if (codeValue == null || codeValue.isBlank()) {
            return noDiscount(context.subtotal());
        }

        DiscountCode code = discountCodeRepository.findByCodeIgnoreCase(codeValue.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid discount code"));

        validateCode(code, context);
        DiscountStrategy strategy = resolveStrategy(code.getType());
        strategy.validate(code, context);
        return strategy.apply(code, context);
    }

    public DiscountCode getAppliedCode(String codeValue) {
        if (codeValue == null || codeValue.isBlank()) {
            return null;
        }
        return discountCodeRepository.findByCodeIgnoreCase(codeValue.trim()).orElse(null);
    }

    private void validateCode(DiscountCode code, DiscountContext context) {
        Instant now = context.bookingTime();

        if (!code.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Discount code is inactive");
        }
        if (now.isBefore(code.getValidFrom()) || now.isAfter(code.getValidUntil())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Discount code is not valid at this time");
        }
        if (code.getMaxUsageCount() != null && code.getCurrentUsageCount() >= code.getMaxUsageCount()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Discount code usage limit reached");
        }
        if (code.getMinOrderAmount() != null && context.subtotal().compareTo(code.getMinOrderAmount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order amount does not meet minimum for discount code");
        }
    }

    private DiscountStrategy resolveStrategy(com.movieticket.domain.enums.DiscountType type) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(type))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unsupported discount type: " + type));
    }

    private DiscountResult noDiscount(BigDecimal subtotal) {
        return new DiscountResult(BigDecimal.ZERO, subtotal, "No discount applied");
    }
}
