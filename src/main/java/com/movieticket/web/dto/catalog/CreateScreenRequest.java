package com.movieticket.web.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateScreenRequest(
        @NotNull UUID theaterId,
        @NotBlank @Size(max = 255) String name,
        @Min(1) int totalRows,
        @Min(1) int totalColumns
) {
}
