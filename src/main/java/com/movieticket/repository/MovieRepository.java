package com.movieticket.repository;

import com.movieticket.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
}
