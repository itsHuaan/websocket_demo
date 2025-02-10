package com.example.websocket_demo.service.chat.impl;

import com.example.websocket_demo.dto.ChatMessageDto;
import com.example.websocket_demo.model.ChatMessageModel;
import com.example.websocket_demo.model.ChatNotificationModel;
import com.example.websocket_demo.service.chat.IChatActionService;
import com.example.websocket_demo.service.chat.IChatMessageService;
import com.example.websocket_demo.service.chat.IChatRoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatActionServiceImpl implements IChatActionService {
    SimpMessagingTemplate messagingTemplate;
    IChatMessageService chatMessageService;
    IChatRoomService chatRoomService;

    @Override
    public void processMessage(ChatMessageModel message) {
        try {
            chatMessageService.saveMessage(message);
            ChatMessageDto chatMessage = chatMessageService.getChatMessageById(message.getId());
            sendMessage(chatMessage);
        } catch (IllegalArgumentException e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }

    public void sendMessage(ChatMessageDto message) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getRecipientId()),
                "/queue/messages",
                ChatNotificationModel.builder()
                        .id(message.getMessageId())
                        .senderId(message.getSenderId())
                        .recipientId(message.getRecipientId())
                        .message(message.getMessage())
                        .mediaUrls(message.getMediaUrls())
                        .sentAt(message.getSentAt())
                        .senderVisibility(message.getSenderVisibility())
                        .recipientVisibility(message.getRecipientVisibility())
                        .build());
    }
}
