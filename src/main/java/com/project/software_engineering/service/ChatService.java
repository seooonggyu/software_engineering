package com.project.software_engineering.service;

import com.project.software_engineering.dto.ChatDto;

import java.util.List;

public interface ChatService {
    ChatDto.ChatRoomRes createOrGetChatRoom(Long user1Id, Long user2Id);
    List<ChatDto.ChatRoomRes> getChatRooms(Long userId);
    List<ChatDto.ChatMessageRes> getChatHistory(Long chatRoomId);
    ChatDto.ChatMessageRes saveMessage(ChatDto.ChatMessageReq req);
    void markMessagesAsRead(Long chatRoomId, Long readerId);
    void leaveChatRoom(Long chatRoomId, Long userId);
}
