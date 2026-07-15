package com.ashish.rentflatmatefinder.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TenantProfileRequest {

    @NotBlank(message = "Preferred city is required")
    private String preferredCity;

    private String preferredLocality;

    @NotNull(message = "Minimum budget is required")
    @DecimalMin(value = "0", message = "Minimum budget must be non-negative")
    private BigDecimal minBudget;

    @NotNull(message = "Maximum budget is required")
    @DecimalMin(value = "0", message = "Maximum budget must be non-negative")
    private BigDecimal maxBudget;

    @NotNull(message = "Move-in date is required")
    @FutureOrPresent(message = "Move-in date must be today or in the future")
    private LocalDate moveInDate;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
