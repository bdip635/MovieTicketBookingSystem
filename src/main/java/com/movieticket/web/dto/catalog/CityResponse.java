package com.movieticket.web.dto.catalog;

import java.time.Instant;
import java.util.UUID;

public record CityResponse(
        UUID id,
        String name,
        String timezone,
        Instant createdAt
) {
}
