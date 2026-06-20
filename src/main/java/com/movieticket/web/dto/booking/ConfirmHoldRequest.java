package com.movieticket.web.dto.booking;

import jakarta.validation.constraints.Size;

public record ConfirmHoldRequest(
        @Size(max = 64) String discountCode
) {
}
