package com.ashish.rentflatmatefinder.repository;

import com.ashish.rentflatmatefinder.entity.ChatRoom;
import com.ashish.rentflatmatefinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.tenant = :user OR cr.owner = :user")
    List<ChatRoom> findByParticipant(@Param("user") User user);
}
