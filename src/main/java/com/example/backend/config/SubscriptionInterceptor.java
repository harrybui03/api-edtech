package com.example.backend.config;

import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInterceptor implements ChannelInterceptor {

    private final EnrollmentService enrollmentService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication user = (Authentication) accessor.getUser();
            String destination = accessor.getDestination();

            if (user != null && destination != null && destination.startsWith("/topic/session/")) {
                String sessionIdStr = destination.substring("/topic/session/".length());
                UUID sessionId;
                try {
                    sessionId = UUID.fromString(sessionIdStr);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid session ID format: {}", sessionIdStr, e);
                    throw new InvalidRequestDataException("Invalid session ID format.");
                }
                String userEmail = user.getName();

                if (!enrollmentService.isUserAuthorizedForSession(userEmail, sessionId)) {
                    throw new ForbiddenException("User " + userEmail + " is not authorized for session " + sessionId);
                }
            }
        }
        return message;
    }
}
