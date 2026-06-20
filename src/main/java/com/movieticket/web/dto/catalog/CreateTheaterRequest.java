package com.movieticket.web.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTheaterRequest(
        @NotNull UUID cityId,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 512) String address
) {
}
