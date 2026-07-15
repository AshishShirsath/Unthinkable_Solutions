package com.ashish.rentflatmatefinder.controller;

import com.ashish.rentflatmatefinder.dto.request.ChatMessageRequest;
import com.ashish.rentflatmatefinder.dto.response.ApiResponse;
import com.ashish.rentflatmatefinder.dto.response.ChatMessageResponse;
import com.ashish.rentflatmatefinder.dto.response.ChatRoomResponse;
import com.ashish.rentflatmatefinder.service.ChatService;
import com.ashish.rentflatmatefinder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms() {
        return ResponseEntity.ok(ApiResponse.success("Chat rooms",
                chatService.getMyChatRooms()));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success("Messages",
                chatService.getMessages(roomId)));
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Message sent",
                chatService.saveMessage(roomId, email, request.getContent())));
    }
}
