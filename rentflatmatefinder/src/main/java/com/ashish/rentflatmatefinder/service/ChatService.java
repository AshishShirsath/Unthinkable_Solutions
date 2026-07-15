package com.ashish.rentflatmatefinder.service;

import com.ashish.rentflatmatefinder.dto.response.ChatMessageResponse;
import com.ashish.rentflatmatefinder.dto.response.ChatRoomResponse;
import com.ashish.rentflatmatefinder.entity.ChatMessage;
import com.ashish.rentflatmatefinder.entity.ChatRoom;
import com.ashish.rentflatmatefinder.entity.User;
import com.ashish.rentflatmatefinder.exception.ResourceNotFoundException;
import com.ashish.rentflatmatefinder.exception.UnauthorizedException;
import com.ashish.rentflatmatefinder.repository.ChatMessageRepository;
import com.ashish.rentflatmatefinder.repository.ChatRoomRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import com.ashish.rentflatmatefinder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyChatRooms() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return chatRoomRepository.findByParticipant(user).stream()
                .map(room -> toRoomResponse(room, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long roomId) {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        if (!room.getTenant().getId().equals(user.getId()) &&
                !room.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not a participant of this chat room");
        }

        return chatMessageRepository.findByChatRoomOrderBySentAtAsc(room).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageResponse saveMessage(Long roomId, String senderEmail, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        if (!room.getTenant().getId().equals(sender.getId()) &&
                !room.getOwner().getId().equals(sender.getId())) {
            throw new UnauthorizedException("You are not a participant of this chat room");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        return toMessageResponse(saved);
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, User currentUser) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderBySentAtAsc(room);
        ChatMessageResponse lastMessage = messages.isEmpty() ? null :
                toMessageResponse(messages.get(messages.size() - 1));

        return ChatRoomResponse.builder()
                .id(room.getId())
                .tenantId(room.getTenant().getId())
                .tenantName(room.getTenant().getFirstName() + " " + room.getTenant().getLastName())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFirstName() + " " + room.getOwner().getLastName())
                .listingId(room.getInterestRequest().getListing().getId())
                .listingTitle(room.getInterestRequest().getListing().getTitle())
                .createdAt(room.getCreatedAt())
                .lastMessage(lastMessage)
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}
