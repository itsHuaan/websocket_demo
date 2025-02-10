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
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(chatMessage.getRecipientId()),
                    "/queue/messages",
                    ChatNotificationModel.builder()
                            .id(chatMessage.getMessageId())
                            .senderId(chatMessage.getSenderId())
                            .recipientId(chatMessage.getRecipientId())
                            .message(chatMessage.getMessage())
                            .mediaUrls(chatMessage.getMediaUrls())
                            .sentAt(chatMessage.getSentAt())
                            .senderVisibility(chatMessage.getSenderVisibility())
                            .recipientVisibility(chatMessage.getRecipientVisibility())
                            .build()
            );
        } catch (IllegalArgumentException e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }
}
