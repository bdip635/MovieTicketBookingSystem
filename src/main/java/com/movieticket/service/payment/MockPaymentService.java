package com.movieticket.service.payment;

import com.movieticket.domain.enums.PaymentStatus;
import com.movieticket.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MockPaymentService {

    public PaymentResult charge(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment amount must be positive");
        }

        return new PaymentResult(
                PaymentStatus.SUCCESS,
                "TXN-" + UUID.randomUUID(),
                null
        );
    }
}
