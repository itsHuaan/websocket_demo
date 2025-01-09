package com.example.websocket_demo.controller;

import com.example.websocket_demo.model.ChatMessageModel;
import com.example.websocket_demo.service.chat.IChatActionService;
import com.example.websocket_demo.util.Const;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Chat Controller")
@RequestMapping(value = Const.API_PREFIX + "/message")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    IChatActionService chatActionService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageModel message) {
        chatActionService.processMessage(message);
    }
}
