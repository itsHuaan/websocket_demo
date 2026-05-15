package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.ChatMessageRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.chat.IChatMessageService;
import com.example.websocket_demo.common.Const;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;

@RestController
@Tag(name = "Chat Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/message")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    IChatMessageService chatMessageService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageRequest message, Principal principal) {
        if (principal != null) {
            message.setSenderId(Long.parseLong(principal.getName()));
        }
        chatMessageService.processMessage(message);
    }

    @MessageMapping("/chat.typing")
    public void processTyping(@Payload Map<String, Object> payload, Principal principal) {
        if (principal != null) {
            String senderId = principal.getName();
            String username = String.valueOf(payload.get("username"));
            String recipientId = String.valueOf(payload.get("recipientId"));
            boolean isTyping = (boolean) payload.get("isTyping");
            
            messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/typing",
                Map.of("senderId", senderId, "username", username, "isTyping", isTyping)
            );
        }
    }

    @MessageMapping("/chat.read")
    public void processRead(@Payload Map<String, Object> payload, Principal principal) {
        if (principal != null) {
            Long senderId = Long.parseLong(principal.getName());
            Long recipientId = Long.valueOf(String.valueOf(payload.get("recipientId")));
            chatMessageService.markMessagesAsRead(senderId, recipientId);
        }
    }

    @GetMapping("/{recipientId}")
    public ResponseEntity<ApiResponse<?>> getChatMessages(
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }
        
        Long senderId = Long.parseLong(principal.getName());
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Chat history retrieved", chatMessageService.getChatMessages(senderId, recipientId, pageable)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "No chat history found", Page.empty(pageable)));
        }
    }
}


