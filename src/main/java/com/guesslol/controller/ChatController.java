package com.guesslol.controller;

import com.guesslol.model.WebSocketMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat/message")
    public void sendMessage(WebSocketMessage message, StompHeaderAccessor accessor) {
        String username = (String) accessor.getSessionAttributes().get("username");
        String roomName = (String) accessor.getSessionAttributes().get("roomName");

        if (username == null || roomName == null || message.getContent() == null || message.getContent().isEmpty()) {
            return;
        }
        message.setUsername(username);
        message.setRoomName(roomName);
        message.setType("chat_message");

        simpMessagingTemplate.convertAndSend("/topic/chat/" + roomName, message);
    }
}
