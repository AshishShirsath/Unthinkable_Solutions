package com.ashish.rentflatmatefinder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "compatibility_scores",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "listing_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false)
    private Integer score;

    @Column(length = 1000)
    private String explanation;

    @Column(nullable = false)
    private Boolean llmUsed;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime computedAt;

    // CompatibilityScore table currently has a NOT NULL `deleted` column (BaseEntity style).
    // Add it here so inserts/updates do not fail with: Column 'deleted' cannot be null.
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
