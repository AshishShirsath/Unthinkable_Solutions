package com.ashish.rentflatmatefinder.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
}
