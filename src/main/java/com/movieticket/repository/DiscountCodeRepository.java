package com.movieticket.repository;

import com.movieticket.domain.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID> {

    Optional<DiscountCode> findByCodeIgnoreCase(String code);
}
