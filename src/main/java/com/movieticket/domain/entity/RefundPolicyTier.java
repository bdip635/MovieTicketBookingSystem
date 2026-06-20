package com.movieticket.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "refund_policy_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicyTier {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refund_policy_id", nullable = false)
    private RefundPolicy refundPolicy;

    @Column(name = "min_hours_before", nullable = false, precision = 10, scale = 2)
    private BigDecimal minHoursBefore;

    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
