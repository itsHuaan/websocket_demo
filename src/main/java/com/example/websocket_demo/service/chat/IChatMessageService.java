package com.example.websocket_demo.service.chat;

import com.example.websocket_demo.dto.ChatMessageDto;
import com.example.websocket_demo.model.ChatMessageModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IChatMessageService {
    ChatMessageDto getChatMessageById(Long id);
    int saveMessage(ChatMessageModel message);
    Page<ChatMessageDto> getChatMessages(Long senderId, Long recipientId, Pageable pageable);
    int deleteMessage(Long senderId, Long recipientId);
}
