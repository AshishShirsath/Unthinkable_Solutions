package com.ashish.rentflatmatefinder.dto.request;

import com.ashish.rentflatmatefinder.entity.FurnishingStatus;
import com.ashish.rentflatmatefinder.entity.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateListingRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String city;

    @NotBlank
    private String locality;

    @NotBlank
    private String address;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal rent;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deposit;

    @NotNull
    @FutureOrPresent
    private LocalDate availableFrom;

    @NotNull
    private RoomType roomType;

    @NotNull
    private FurnishingStatus furnishingStatus;

    private List<String> imageUrls;

}