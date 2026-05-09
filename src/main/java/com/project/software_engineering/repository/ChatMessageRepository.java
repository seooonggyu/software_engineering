package com.project.software_engineering.repository;

import com.project.software_engineering.domain.ChatMessage;
import com.project.software_engineering.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomAndDeletedFalseOrderByCreatedAtAsc(ChatRoom chatRoom);
}
