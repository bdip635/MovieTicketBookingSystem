package com.movieticket.repository;

import com.movieticket.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
