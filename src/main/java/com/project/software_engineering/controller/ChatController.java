package com.project.software_engineering.controller;

import com.project.software_engineering.dto.ChatDto;
import com.project.software_engineering.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.project.software_engineering.security.PrincipalDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    @PostMapping("/room")
    public ResponseEntity<ChatDto.ChatRoomRes> createOrGetRoom(
            @AuthenticationPrincipal PrincipalDetails principalDetails, 
            @RequestParam Long otherUserId) {
        Long myUserId = principalDetails.getUser().getId();
        return ResponseEntity.ok(chatService.createOrGetChatRoom(myUserId, otherUserId));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatDto.ChatRoomRes>> getChatRooms(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long myUserId = principalDetails.getUser().getId();
        return ResponseEntity.ok(chatService.getChatRooms(myUserId));
    }

    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<ChatDto.ChatMessageRes>> getChatHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getChatHistory(roomId));
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> leaveRoom(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long roomId) {
        Long myUserId = principalDetails.getUser().getId();
        chatService.leaveChatRoom(roomId, myUserId);
        
        ChatDto.ChatMessageRes leaveNotification = ChatDto.ChatMessageRes.builder()
                .type("LEAVE")
                .chatRoomId(roomId)
                .senderId(myUserId)
                .message("상대방이 채팅방을 나갔습니다.")
                .build();
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, leaveNotification);
        
        return ResponseEntity.ok().build();
    }
}
