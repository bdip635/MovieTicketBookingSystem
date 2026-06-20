package com.movieticket.service.refund;

import com.movieticket.domain.entity.RefundPolicy;
import com.movieticket.domain.entity.RefundPolicyTier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RefundPolicyEngineTest {

    private final RefundPolicyEngine engine = new RefundPolicyEngine();

    @Test
    void appliesFullRefundMoreThan24HoursBeforeShow() {
        RefundPolicy policy = standardPolicy();
        Instant showStart = Instant.now().plus(30, ChronoUnit.HOURS);

        RefundCalculation result = engine.compute(policy, showStart, Instant.now());

        assertEquals(0, result.refundPercentage().compareTo(new BigDecimal("100.00")));
        assertEquals(0, engine.calculateRefundAmount(new BigDecimal("300.00"), result.refundPercentage())
                .compareTo(new BigDecimal("300.00")));
    }

    @Test
    void appliesPartialRefundBetween2And24Hours() {
        RefundPolicy policy = standardPolicy();
        Instant showStart = Instant.now().plus(10, ChronoUnit.HOURS);

        RefundCalculation result = engine.compute(policy, showStart, Instant.now());

        assertEquals(0, result.refundPercentage().compareTo(new BigDecimal("50.00")));
        assertEquals(0, engine.calculateRefundAmount(new BigDecimal("300.00"), result.refundPercentage())
                .compareTo(new BigDecimal("150.00")));
    }

    @Test
    void appliesNoRefundWithin2Hours() {
        RefundPolicy policy = standardPolicy();
        Instant showStart = Instant.now().plus(90, ChronoUnit.MINUTES);

        RefundCalculation result = engine.compute(policy, showStart, Instant.now());

        assertEquals(0, result.refundPercentage().compareTo(new BigDecimal("0.00")));
    }

    private RefundPolicy standardPolicy() {
        RefundPolicy policy = RefundPolicy.builder().name("Standard").active(true).build();
        policy.addTier(RefundPolicyTier.builder().minHoursBefore(new BigDecimal("24")).refundPercentage(new BigDecimal("100")).sortOrder(1).build());
        policy.addTier(RefundPolicyTier.builder().minHoursBefore(new BigDecimal("2")).refundPercentage(new BigDecimal("50")).sortOrder(2).build());
        policy.addTier(RefundPolicyTier.builder().minHoursBefore(new BigDecimal("0")).refundPercentage(new BigDecimal("0")).sortOrder(3).build());
        return policy;
    }
}
