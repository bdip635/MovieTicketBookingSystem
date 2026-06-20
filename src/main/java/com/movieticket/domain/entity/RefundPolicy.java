package com.movieticket.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "refund_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicy {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "refundPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<RefundPolicyTier> tiers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public void addTier(RefundPolicyTier tier) {
        tiers.add(tier);
        tier.setRefundPolicy(this);
    }
}
