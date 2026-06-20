package com.movieticket.repository;

import com.movieticket.domain.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, UUID> {

    List<RefundPolicy> findByTheaterId(UUID theaterId);

    Optional<RefundPolicy> findFirstByTheaterIdAndActiveTrueOrderByCreatedAtDesc(UUID theaterId);
}
