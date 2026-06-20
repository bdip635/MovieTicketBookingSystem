package com.movieticket.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "regular_base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal regularBasePrice;

    @Column(name = "premium_base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumBasePrice;

    @Column(name = "weekend_multiplier", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal weekendMultiplier = new BigDecimal("1.20");

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
