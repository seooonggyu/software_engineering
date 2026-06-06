package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.ChatMessage;
import com.project.software_engineering.domain.ChatRoom;
import com.project.software_engineering.domain.User;
import com.project.software_engineering.dto.ChatDto;
import com.project.software_engineering.repository.ChatMessageRepository;
import com.project.software_engineering.repository.ChatRoomRepository;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatDto.ChatRoomRes createOrGetChatRoom(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElseThrow(() -> new IllegalArgumentException("User not found: " + user1Id));
        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new IllegalArgumentException("User not found: " + user2Id));

        ChatRoom chatRoom = chatRoomRepository.findByUser1AndUser2(user1, user2)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.of(user1, user2)));

        return ChatDto.ChatRoomRes.builder()
                .id(chatRoom.getId())
                .otherUserId(user2.getId())
                .otherUsername(user2.getUsername())
                .otherName(user2.getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto.ChatRoomRes> getChatRooms(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        List<ChatRoom> chatRooms = chatRoomRepository.findByUser(user);

        return chatRooms.stream().map(room -> {
            User otherUser = room.getUser1().getId().equals(userId) ? room.getUser2() : room.getUser1();
            int unread = (int) chatMessageRepository.countUnread(room, userId);
            ChatMessage lastMessage = chatMessageRepository
                    .findFirstByChatRoomAndDeletedFalseOrderByCreatedAtDesc(room)
                    .orElse(null);
            return ChatDto.ChatRoomRes.builder()
                    .id(room.getId())
                    .otherUserId(otherUser.getId())
                    .otherUsername(otherUser.getUsername())
                    .otherName(otherUser.getName())
                    .unreadCount(unread)
                    .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                    .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto.ChatMessageRes> getChatHistory(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId));
        return chatMessageRepository.findByChatRoomAndDeletedFalseOrderByCreatedAtAsc(chatRoom).stream()
                .map(msg -> ChatDto.ChatMessageRes.builder()
                        .type("CHAT")
                        .id(msg.getId())
                        .chatRoomId(msg.getChatRoom().getId())
                        .senderId(msg.getSender().getId())
                        .senderUsername(msg.getSender().getUsername())
                        .senderName(msg.getSender().getName())
                        .message(msg.getMessage())
                        .isRead(msg.isRead())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatDto.ChatMessageRes saveMessage(ChatDto.ChatMessageReq req) {
        ChatRoom chatRoom = chatRoomRepository.findById(req.getChatRoomId()).orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + req.getChatRoomId()));
        User sender = userRepository.findById(req.getSenderId()).orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getSenderId()));

        ChatMessage chatMessage = ChatMessage.of(chatRoom, sender, req.getMessage());
        chatMessageRepository.save(chatMessage);
        
        LocalDateTime createdAt = chatMessage.getCreatedAt() != null ? chatMessage.getCreatedAt() : LocalDateTime.now();

        return ChatDto.ChatMessageRes.builder()
                .type("CHAT")
                .id(chatMessage.getId())
                .chatRoomId(chatRoom.getId())
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .senderName(sender.getName())
                .message(chatMessage.getMessage())
                .isRead(chatMessage.isRead())
                .createdAt(createdAt)
                .build();
    }

    @Override
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId));
        
        if (!chatRoom.getUser1().getId().equals(userId) && !chatRoom.getUser2().getId().equals(userId)) {
            throw new IllegalArgumentException("Not a participant of this chat room");
        }

        chatRoom.setDeleted(true);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long chatRoomId, Long readerId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId));
        
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoomAndDeletedFalseOrderByCreatedAtAsc(chatRoom).stream()
                .filter(msg -> !msg.getSender().getId().equals(readerId) && !msg.isRead())
                .collect(Collectors.toList());
        
        unreadMessages.forEach(msg -> msg.setRead(true));
    }
}
