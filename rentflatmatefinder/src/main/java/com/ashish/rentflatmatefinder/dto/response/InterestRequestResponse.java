package com.ashish.rentflatmatefinder.dto.response;

import com.ashish.rentflatmatefinder.entity.InterestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRequestResponse {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private Long listingId;
    private String listingTitle;
    private String listingCity;
    private InterestStatus status;
    private Integer compatibilityScore;
    private String scoreExplanation;
    private String message;
    private Long chatRoomId;
    private LocalDateTime createdAt;
}
