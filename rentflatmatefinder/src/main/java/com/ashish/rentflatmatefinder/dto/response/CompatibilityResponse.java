package com.ashish.rentflatmatefinder.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityResponse {
    private Long listingId;
    private Integer score;
    private String explanation;
    private Boolean llmUsed;
}
