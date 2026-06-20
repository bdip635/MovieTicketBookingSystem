package com.movieticket.web.dto.booking;

import com.movieticket.domain.enums.BookingStatus;
import com.movieticket.domain.enums.RefundStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CancelBookingResponse(
        UUID bookingId,
        BookingStatus status,
        BigDecimal paidAmount,
        BigDecimal refundAmount,
        BigDecimal refundPercentage,
        RefundStatus refundStatus
) {
}
