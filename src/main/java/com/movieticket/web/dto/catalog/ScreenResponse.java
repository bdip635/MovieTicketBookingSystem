package com.movieticket.web.dto.catalog;

import java.time.Instant;
import java.util.UUID;

public record ScreenResponse(
        UUID id,
        UUID theaterId,
        String theaterName,
        String name,
        int totalRows,
        int totalColumns,
        Instant createdAt
) {
}
