package com.project.software_engineering.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class ChatDto {

    @Getter
    @Setter
    public static class ChatRoomRes {
        private Long id;
        private Long otherUserId;
        private String otherUsername;
        // 주의: 필드명을 otherUserName 으로 두면 otherUsername 과 대소문자 한 글자만 달라
        // Lombok + Jackson 직렬화 시 두 속성이 충돌해 이름이 누락된다. otherName 으로 분리.
        private String otherName;
        private int unreadCount;
        private String lastMessage;
        private LocalDateTime lastMessageAt;

        @Builder
        public ChatRoomRes(Long id, Long otherUserId, String otherUsername, String otherName, int unreadCount, String lastMessage, LocalDateTime lastMessageAt) {
            this.id = id;
            this.otherUserId = otherUserId;
            this.otherUsername = otherUsername;
            this.otherName = otherName;
            this.unreadCount = unreadCount;
            this.lastMessage = lastMessage;
            this.lastMessageAt = lastMessageAt;
        }
    }

    @Getter
    @Setter
    public static class ChatMessageReq {
        private String type; // "CHAT", "READ", "LEAVE"
        private Long chatRoomId;
        private Long senderId;
        private String message;
    }

    @Getter
    @Setter
    public static class ChatMessageRes {
        private String type; // "CHAT", "READ", "LEAVE"
        private Long id;
        private Long chatRoomId;
        private Long senderId;
        private String senderUsername;
        private String senderName;
        private String message;
        private boolean isRead;
        private LocalDateTime createdAt;

        @Builder
        public ChatMessageRes(String type, Long id, Long chatRoomId, Long senderId, String senderUsername, String senderName, String message, boolean isRead, LocalDateTime createdAt) {
            this.type = type;
            this.id = id;
            this.chatRoomId = chatRoomId;
            this.senderId = senderId;
            this.senderUsername = senderUsername;
            this.senderName = senderName;
            this.message = message;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }
    }
}
