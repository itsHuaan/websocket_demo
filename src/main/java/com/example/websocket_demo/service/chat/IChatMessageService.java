package com.example.websocket_demo.service.chat;

import com.example.websocket_demo.dto.response.ChatMessageResponse;
import com.example.websocket_demo.dto.request.ChatMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IChatMessageService {
    ChatMessageResponse getChatMessageById(Long id);
    ChatMessageResponse saveMessage(ChatMessageRequest message);
    Page<ChatMessageResponse> getChatMessages(Long senderId, Long recipientId, Pageable pageable);
    void deleteMessage(Long senderId, Long recipientId);
    void removeMessageForMe(Long userId, Long messageId);
    void removeMessageForEveryone(Long userId, Long messageId);
    void processMessage(ChatMessageRequest message);
    void markMessagesAsRead(Long senderId, Long recipientId);
}


