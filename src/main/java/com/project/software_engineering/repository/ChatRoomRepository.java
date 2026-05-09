package com.project.software_engineering.repository;

import com.project.software_engineering.domain.ChatRoom;
import com.project.software_engineering.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    @Query("SELECT c FROM ChatRoom c WHERE c.deleted = false AND ((c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1))")
    Optional<ChatRoom> findByUser1AndUser2(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT c FROM ChatRoom c WHERE c.deleted = false AND (c.user1 = :user OR c.user2 = :user)")
    List<ChatRoom> findByUser(@Param("user") User user);
}
