package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.ChatMessage;
import com.ashish.rentflatmatefinder.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);
}
