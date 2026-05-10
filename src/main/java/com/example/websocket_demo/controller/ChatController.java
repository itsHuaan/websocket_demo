package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.ChatMessageRequest;
import com.example.websocket_demo.service.chat.IChatMessageService;
import com.example.websocket_demo.common.Const;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@Tag(name = "Chat Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/message")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    IChatMessageService chatMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageRequest message, Principal principal) {
        if (principal != null) {
            message.setSenderId(Long.parseLong(principal.getName()));
        }
        chatMessageService.processMessage(message);
    }
}


