package com.movieticket.domain.entity;

import com.movieticket.domain.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "discount_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCode {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType type;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "max_usage_count")
    private Integer maxUsageCount;

    @Column(name = "current_usage_count", nullable = false)
    @Builder.Default
    private int currentUsageCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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
