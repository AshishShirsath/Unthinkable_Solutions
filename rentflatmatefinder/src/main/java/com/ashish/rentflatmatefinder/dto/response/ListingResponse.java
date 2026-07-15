package com.ashish.rentflatmatefinder.dto.response;

import com.ashish.rentflatmatefinder.entity.FurnishingStatus;
import com.ashish.rentflatmatefinder.entity.ListingStatus;
import com.ashish.rentflatmatefinder.entity.RoomType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingResponse {

    private Long id;

    private String title;

    private String description;

    private String city;

    private String locality;

    private String address;

    private BigDecimal rent;

    private BigDecimal deposit;

    private LocalDate availableFrom;

    private RoomType roomType;

    private FurnishingStatus furnishingStatus;

    private ListingStatus status;

    private String ownerName;

    private String ownerEmail;

    private List<String> imageUrls;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}