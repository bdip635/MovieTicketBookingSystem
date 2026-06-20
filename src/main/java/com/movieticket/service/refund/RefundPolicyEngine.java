package com.movieticket.service.refund;

import com.movieticket.domain.entity.RefundPolicy;
import com.movieticket.domain.entity.RefundPolicyTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

@Component
public class RefundPolicyEngine {

    public RefundCalculation compute(RefundPolicy policy, Instant showStartTime, Instant cancellationTime) {
        if (policy == null || policy.getTiers().isEmpty()) {
            return new RefundCalculation(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal hoursBeforeShow = hoursBetween(cancellationTime, showStartTime);
        if (hoursBeforeShow.compareTo(BigDecimal.ZERO) < 0) {
            return new RefundCalculation(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal refundPercentage = policy.getTiers().stream()
                .sorted(Comparator.comparing(RefundPolicyTier::getMinHoursBefore).reversed())
                .filter(tier -> hoursBeforeShow.compareTo(tier.getMinHoursBefore()) >= 0)
                .findFirst()
                .map(RefundPolicyTier::getRefundPercentage)
                .orElse(BigDecimal.ZERO);

        return new RefundCalculation(refundPercentage, hoursBeforeShow);
    }

    public BigDecimal calculateRefundAmount(BigDecimal paidAmount, BigDecimal refundPercentage) {
        return paidAmount
                .multiply(refundPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal hoursBetween(Instant from, Instant to) {
        long minutes = Duration.between(from, to).toMinutes();
        return BigDecimal.valueOf(minutes)
                .divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
    }
}
