package com.movieticket.web.dto.catalog;

import java.time.Instant;
import java.util.UUID;

public record MovieResponse(
        UUID id,
        String title,
        String description,
        int durationMinutes,
        String language,
        Instant createdAt
) {
}
