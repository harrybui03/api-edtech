package com.example.backend.config;

import com.example.backend.dto.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // Retrieve the username and sessionId from the WebSocket session attributes
        String username = (String) accessor.getSessionAttributes().get("username");
        UUID sessionId = (UUID) accessor.getSessionAttributes().get("sessionId");

        if (username != null && sessionId != null) {
            // Create a LEAVE message
            var chatMessage = ChatMessage.builder()
                    .type(ChatMessage.MessageType.LEAVE)
                    .sender(username)
                    .content(username + " left!")
                    .build();

            // Broadcast the leave message to the specific session topic
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, chatMessage);
        }
    }
}
