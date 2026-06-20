package com.movieticket.service.payment;

import com.movieticket.domain.enums.PaymentStatus;

public record PaymentResult(
        PaymentStatus status,
        String transactionId,
        String failureReason
) {
}
