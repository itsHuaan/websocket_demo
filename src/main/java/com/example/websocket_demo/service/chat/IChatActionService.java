package com.example.websocket_demo.service.chat;

import com.example.websocket_demo.model.ChatMessageModel;

public interface IChatActionService {
    void processMessage(ChatMessageModel message);
}
