package com.ashish.rentflatmatefinder.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterestRequestDto {

    @NotNull(message = "Listing ID is required")
    private Long listingId;

    @Size(max = 500, message = "Message must be at most 500 characters")
    private String message;
}
