package com.ashish.rentflatmatefinder.entity;

import com.ashish.rentflatmatefinder.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "interest_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InterestRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestStatus status;

    @Column
    private Integer compatibilityScore;

    @Column(length = 1000)
    private String scoreExplanation;

    @Column(length = 500)
    private String message;

    @OneToOne(mappedBy = "interestRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatRoom chatRoom;
}
