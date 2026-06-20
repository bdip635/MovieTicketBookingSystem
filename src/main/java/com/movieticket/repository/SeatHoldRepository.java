package com.movieticket.repository;

import com.movieticket.domain.entity.SeatHold;
import com.movieticket.domain.enums.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {

    List<SeatHold> findByStatusAndExpiresAtBefore(HoldStatus status, Instant expiresAt);

    Optional<SeatHold> findByIdAndUserId(UUID id, UUID userId);
}
