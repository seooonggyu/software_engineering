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
        private String otherUserName;
        private int unreadCount;

        @Builder
        public ChatRoomRes(Long id, Long otherUserId, String otherUsername, String otherUserName, int unreadCount) {
            this.id = id;
            this.otherUserId = otherUserId;
            this.otherUsername = otherUsername;
            this.otherUserName = otherUserName;
            this.unreadCount = unreadCount;
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
        private String message;
        private boolean isRead;
        private LocalDateTime createdAt;
        
        @Builder
        public ChatMessageRes(String type, Long id, Long chatRoomId, Long senderId, String senderUsername, String message, boolean isRead, LocalDateTime createdAt) {
            this.type = type;
            this.id = id;
            this.chatRoomId = chatRoomId;
            this.senderId = senderId;
            this.senderUsername = senderUsername;
            this.message = message;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }
    }
}
