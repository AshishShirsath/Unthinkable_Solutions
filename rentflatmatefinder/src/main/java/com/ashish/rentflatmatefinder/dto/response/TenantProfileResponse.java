package com.ashish.rentflatmatefinder.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProfileResponse {
    private Long id;
    private Long userId;
    private String preferredCity;
    private String preferredLocality;
    private BigDecimal minBudget;
    private BigDecimal maxBudget;
    private LocalDate moveInDate;
    private String description;
    private LocalDateTime createdAt;
}
