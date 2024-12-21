package com.guesslol.websocketconfig;

import com.guesslol.service.RoundService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    private final RoundService roundService;

    public WebSocketEventListener(RoundService roundService) {
        this.roundService = roundService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // username and roomname from headers
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomName = (String) headerAccessor.getSessionAttributes().get("roomName");

        if (username != null && roomName != null) {
            roundService.removeUser(username,roomName);
        }
    }
}