package com.movieticket.repository;

import com.movieticket.domain.entity.ShowSeat;
import com.movieticket.domain.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, UUID> {

    List<ShowSeat> findByShowIdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(UUID showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.id IN :ids ORDER BY ss.id")
    List<ShowSeat> findByIdInForUpdate(@Param("ids") List<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds ORDER BY ss.id")
    List<ShowSeat> findByShowIdAndSeatIdInForUpdate(
            @Param("showId") UUID showId,
            @Param("seatIds") List<UUID> seatIds);

    @Query("""
            SELECT ss FROM ShowSeat ss
            WHERE ss.status = :status
              AND ss.holdExpiresAt IS NOT NULL
              AND ss.holdExpiresAt < :now
            """)
    List<ShowSeat> findExpiredHolds(@Param("status") SeatStatus status, @Param("now") Instant now);

    Optional<ShowSeat> findByShowIdAndSeatId(UUID showId, UUID seatId);

    List<ShowSeat> findBySeatHold_Id(UUID seatHoldId);
}
