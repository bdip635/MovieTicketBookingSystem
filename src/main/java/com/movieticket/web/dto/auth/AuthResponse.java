package com.movieticket.web.dto.auth;

import com.movieticket.domain.enums.Role;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String email,
        String fullName,
        Role role
) {
}
