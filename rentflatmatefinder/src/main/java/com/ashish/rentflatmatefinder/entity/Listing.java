package com.ashish.rentflatmatefinder.entity;

import com.ashish.rentflatmatefinder.audit.BaseEntity;
import com.ashish.rentflatmatefinder.entity.FurnishingStatus;
import com.ashish.rentflatmatefinder.entity.ListingStatus;
import com.ashish.rentflatmatefinder.entity.RoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name="listings")
public class Listing extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String locality;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private BigDecimal rent;

    @Column(nullable = false)
    private BigDecimal deposit;

    @Column(nullable = false)
    private LocalDate availableFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FurnishingStatus furnishingStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(
            mappedBy = "listing",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ListingImage> images = new ArrayList<>();

}