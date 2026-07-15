package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.request.ChatMessageRequest;
import com.ashish.rentflatmatefinder.dto.response.ChatMessageResponse;
import com.ashish.rentflatmatefinder.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * Clients send messages to /app/chat/{roomId}
     * All participants subscribed to /topic/chat/{roomId} receive the message
     */
    @MessageMapping("/chat")
    public void sendMessage(ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            return;
        }

        ChatMessageResponse savedMessage = chatService.saveMessage(
                request.getRoomId(),
                principal.getName(),
                request.getContent()
        );

        // Broadcast to all subscribers of this room's topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getRoomId(),
                savedMessage
        );
    }
}
