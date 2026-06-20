package com.movieticket.web.dto.booking;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateHoldRequest(
        @NotEmpty List<UUID> seatIds
) {
}
