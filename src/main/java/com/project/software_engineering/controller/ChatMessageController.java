package com.project.software_engineering.controller;

import com.project.software_engineering.dto.ChatDto;
import com.project.software_engineering.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatDto.ChatMessageReq messageReq) {
        ChatDto.ChatMessageRes savedMessage = chatService.saveMessage(messageReq);
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageReq.getChatRoomId(), savedMessage);
    }

    @MessageMapping("/chat/read")
    public void readMessage(ChatDto.ChatMessageReq readReq) {
        chatService.markMessagesAsRead(readReq.getChatRoomId(), readReq.getSenderId());
        
        ChatDto.ChatMessageRes readNotification = ChatDto.ChatMessageRes.builder()
                .type("READ")
                .chatRoomId(readReq.getChatRoomId())
                .senderId(readReq.getSenderId())
                .build();
                
        messagingTemplate.convertAndSend("/sub/chat/room/" + readReq.getChatRoomId(), readNotification);
    }
}
