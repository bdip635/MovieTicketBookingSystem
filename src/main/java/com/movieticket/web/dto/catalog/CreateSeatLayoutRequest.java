package com.movieticket.web.dto.catalog;

import com.movieticket.domain.enums.SeatTier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateSeatLayoutRequest(
        @NotEmpty List<@Valid SeatDefinition> seats
) {
    public record SeatDefinition(
            @NotBlank String rowLabel,
            @Min(1) int seatNumber,
            @NotNull SeatTier tier
    ) {
    }
}
