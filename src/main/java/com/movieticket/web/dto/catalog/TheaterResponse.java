package com.movieticket.web.dto.catalog;

import java.time.Instant;
import java.util.UUID;

public record TheaterResponse(
        UUID id,
        UUID cityId,
        String cityName,
        String name,
        String address,
        Instant createdAt
) {
}
