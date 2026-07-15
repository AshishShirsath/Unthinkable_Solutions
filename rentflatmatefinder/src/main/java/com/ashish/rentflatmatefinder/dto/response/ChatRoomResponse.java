package com.ashish.rentflatmatefinder.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long ownerId;
    private String ownerName;
    private Long listingId;
    private String listingTitle;
    private LocalDateTime createdAt;
    private ChatMessageResponse lastMessage;
}
