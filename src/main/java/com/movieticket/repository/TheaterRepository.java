package com.movieticket.repository;

import com.movieticket.domain.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TheaterRepository extends JpaRepository<Theater, UUID> {

    List<Theater> findByCityId(UUID cityId);
}
