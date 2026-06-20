package com.movieticket.repository;

import com.movieticket.domain.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {

    @Query("""
            SELECT s FROM Show s
            JOIN s.screen sc
            JOIN sc.theater t
            WHERE t.city.id = :cityId
              AND s.startTime >= :from
              AND s.startTime < :to
            ORDER BY s.startTime ASC
            """)
    List<Show> findByCityAndStartTimeBetween(
            @Param("cityId") UUID cityId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("""
            SELECT s FROM Show s
            WHERE s.startTime >= :from
              AND s.startTime < :to
            ORDER BY s.startTime ASC
            """)
    List<Show> findByStartTimeBetween(@Param("from") Instant from, @Param("to") Instant to);
}
