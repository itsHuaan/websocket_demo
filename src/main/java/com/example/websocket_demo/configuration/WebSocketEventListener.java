package com.example.websocket_demo.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private static final String ONLINE_USERS_KEY = "chat:online_users";

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String userId = headerAccessor.getUser().getName();
            log.info("User connected: {}", userId);
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
            broadcastStatus(userId, true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String userId = headerAccessor.getUser().getName();
            log.info("User disconnected: {}", userId);
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
            broadcastStatus(userId, false);
        }
    }

    private void broadcastStatus(String userId, boolean isOnline) {
        messagingTemplate.convertAndSend("/topic/public.status", 
            Map.of("userId", userId, "status", isOnline ? "ONLINE" : "OFFLINE"));
    }
}
