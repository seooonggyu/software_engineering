package com.project.software_engineering.repository;

import com.project.software_engineering.domain.ChatMessage;
import com.project.software_engineering.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomAndDeletedFalseOrderByCreatedAtAsc(ChatRoom chatRoom);

    Optional<ChatMessage> findFirstByChatRoomAndDeletedFalseOrderByCreatedAtDesc(ChatRoom chatRoom);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :room AND m.deleted = false AND m.sender.id != :userId AND m.isRead = false")
    long countUnread(@Param("room") ChatRoom room, @Param("userId") Long userId);
}
