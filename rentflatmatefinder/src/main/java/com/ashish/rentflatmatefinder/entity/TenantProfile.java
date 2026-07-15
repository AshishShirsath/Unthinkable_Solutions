package com.ashish.rentflatmatefinder.entity;

import com.ashish.rentflatmatefinder.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tenant_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TenantProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String preferredCity;

    @Column
    private String preferredLocality;

    @Column(nullable = false)
    private BigDecimal minBudget;

    @Column(nullable = false)
    private BigDecimal maxBudget;

    @Column(nullable = false)
    private LocalDate moveInDate;

    @Column(length = 1000)
    private String description;
}
