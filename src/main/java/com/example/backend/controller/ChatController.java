package com.example.backend.controller;

import com.example.backend.dto.message.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.UUID;

@Controller
public class ChatController {

    @MessageMapping("/chat.addUser/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage addUser(@DestinationVariable UUID sessionId, @Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("sessionId", sessionId);
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        return chatMessage;
    }

    @MessageMapping("/chat.sendMessage/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage sendMessage(@DestinationVariable UUID sessionId, @Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        return chatMessage;
    }

    @MessageMapping("/chat.raiseHand/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage raiseHand(@DestinationVariable UUID sessionId, @Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.RAISE_HAND);
        chatMessage.setContent(chatMessage.getSender() + " raised their hand!");
        return chatMessage;
    }

    @MessageMapping("/chat.lowerHand/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage lowerHand(@DestinationVariable UUID sessionId, @Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.LOWER_HAND);
        chatMessage.setContent(chatMessage.getSender() + " lowered their hand.");
        return chatMessage;
    }
}
