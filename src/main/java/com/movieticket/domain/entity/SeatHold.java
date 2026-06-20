package com.movieticket.domain.entity;

import com.movieticket.domain.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "seat_holds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHold {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HoldStatus status = HoldStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @OneToMany(mappedBy = "seatHold", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SeatHoldItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public void addItem(SeatHoldItem item) {
        items.add(item);
        item.setSeatHold(this);
    }
}
