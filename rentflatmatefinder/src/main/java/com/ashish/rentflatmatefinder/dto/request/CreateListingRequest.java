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
public class CreateListingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Locality is required")
    private String locality;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Rent is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal rent;

    @NotNull(message = "Deposit is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deposit;

    @NotNull(message = "Available date is required")
    @FutureOrPresent(message = "Available date cannot be in the past")
    private LocalDate availableFrom;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Furnishing status is required")
    private FurnishingStatus furnishingStatus;

    private List<String> imageUrls;

}