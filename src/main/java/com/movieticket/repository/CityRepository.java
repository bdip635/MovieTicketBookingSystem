package com.movieticket.repository;

import com.movieticket.domain.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
}
