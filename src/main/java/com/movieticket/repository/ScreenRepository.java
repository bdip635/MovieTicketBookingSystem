package com.movieticket.repository;

import com.movieticket.domain.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScreenRepository extends JpaRepository<Screen, UUID> {

    List<Screen> findByTheaterId(UUID theaterId);
}
