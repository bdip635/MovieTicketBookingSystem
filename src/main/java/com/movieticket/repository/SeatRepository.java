package com.movieticket.repository;

import com.movieticket.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(UUID screenId);
}
