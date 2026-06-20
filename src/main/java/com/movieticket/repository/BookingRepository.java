package com.movieticket.repository;

import com.movieticket.domain.entity.Booking;
import com.movieticket.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.show s
            JOIN FETCH b.user u
            WHERE b.status = :status
              AND s.startTime >= :startFrom
              AND s.startTime < :startTo
            """)
    List<Booking> findByStatusAndShowStartTimeBetween(
            @Param("status") BookingStatus status,
            @Param("startFrom") Instant startFrom,
            @Param("startTo") Instant startTo);
}
