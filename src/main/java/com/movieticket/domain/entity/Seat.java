package com.movieticket.domain.entity;

import com.movieticket.domain.enums.SeatTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "row_label", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatTier tier;

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
