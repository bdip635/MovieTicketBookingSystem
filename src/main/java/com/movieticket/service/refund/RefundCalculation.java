package com.movieticket.service.refund;

import java.math.BigDecimal;

public record RefundCalculation(
        BigDecimal refundPercentage,
        BigDecimal hoursBeforeShow
) {
}
