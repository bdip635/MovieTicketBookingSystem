package com.movieticket.web.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCityRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 64) String timezone
) {
}
