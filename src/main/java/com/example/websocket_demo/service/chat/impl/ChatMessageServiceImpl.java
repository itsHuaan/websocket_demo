package com.example.websocket_demo.service.chat.impl;

import com.example.websocket_demo.dto.request.ChatMessageRequest;
import com.example.websocket_demo.dto.response.ChatMessageResponse;
import com.example.websocket_demo.dto.response.ChatNotificationResponse;
import com.example.websocket_demo.entity.ChatMessageEntity;
import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.mapper.ChatMapper;
import com.example.websocket_demo.repository.IChatMessageRepository;
import com.example.websocket_demo.repository.IChatRoomRepository;
import com.example.websocket_demo.service.chat.IChatMessageService;
import com.example.websocket_demo.service.chat.IChatRoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageServiceImpl implements IChatMessageService {
    IChatMessageRepository chatMessageRepository;
    ChatMapper chatMapper;
    IChatRoomService chatRoomService;
    IChatRoomRepository chatRoomRepository;
    SimpMessagingTemplate messagingTemplate;

    @Override
    public ChatMessageResponse getChatMessageById(Long id) {
        return chatMessageRepository.findById(id)
                .map(chatMapper::toChatMessageDto)
                .orElseThrow(() -> new NoSuchElementException("Message not found"));
    }

    @Override
    public ChatMessageResponse saveMessage(ChatMessageRequest message) {
        if (Objects.equals(message.getSenderId(), message.getRecipientId())) {
            throw new IllegalArgumentException("You can't send messages to yourself");
        }
        var chatRoomId = chatRoomService.getChatRoomId(message.getSenderId(),
                message.getRecipientId(),
                true).orElseThrow(() -> new IllegalArgumentException("Can't find chat room"));
        message.setChatId(chatRoomId);
        ChatMessageEntity entity = chatMapper.toChatMessageEntity(message);
        applyReplySnapshot(entity, message, chatRoomId);
        chatMessageRepository.save(entity);
        return chatMapper.toChatMessageDto(entity);
    }

    // Build a denormalized snapshot of the message being replied to, so the
    // reply preview survives reloads and original deletion. Only honors replies
    // that point at an existing message in the same conversation.
    private void applyReplySnapshot(ChatMessageEntity entity, ChatMessageRequest message, String chatRoomId) {
        entity.setReplyToMessageId(null);
        entity.setReplyToSenderId(null);
        entity.setReplyToSnippet(null);

        Long replyToId = message.getReplyToMessageId();
        if (replyToId == null) {
            return;
        }
        chatMessageRepository.findById(replyToId)
                .filter(original -> Objects.equals(original.getChatId(), chatRoomId))
                .ifPresent(original -> {
                    entity.setReplyToMessageId(original.getChatMessageId());
                    entity.setReplyToSenderId(original.getSenderId());
                    entity.setReplyToSnippet(buildReplySnippet(original));
                });
    }

    private static String buildReplySnippet(ChatMessageEntity original) {
        String text = original.getMessage();
        if (text != null && !text.isBlank()) {
            String trimmed = text.strip();
            return trimmed.length() > 140 ? trimmed.substring(0, 140) + "…" : trimmed;
        }
        boolean hasMedia = original.getChatMedias() != null && !original.getChatMedias().isEmpty();
        return hasMedia ? "📷 Media" : "";
    }

    @Override
    public Page<ChatMessageResponse> getChatMessages(Long senderId, Long recipientId, Pageable pageable) {
        return chatRoomService.getChatRoomId(senderId, recipientId, false)
                .map(chatId -> chatMessageRepository.findAllByChatIdOrderByCreatedAtDesc(chatId, pageable)
                        .map(chatMapper::toChatMessageDto))
                .orElseThrow(() -> new NoSuchElementException("Chat room not found"));
    }

    @Override
    public void deleteMessage(Long senderId, Long recipientId) {
        var chatRoomId = chatRoomService.getChatRoomId(senderId, recipientId, false)
                .orElseThrow(() -> new NoSuchElementException("Chat room not found"));
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findChatRoomEntitiesByChatId(chatRoomId);
        chatRoomRepository.deleteAll(chatRooms);
        List<ChatMessageEntity> chatMessages = chatMessageRepository.findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(chatRoomId);
        chatMessageRepository.deleteAll(chatMessages);
    }

    @Override
    public void processMessage(ChatMessageRequest message) {
        ChatMessageResponse savedMessage = saveMessage(message);
        log.info("Incoming message from {} to {} in {}: {}", message.getSenderId(), message.getRecipientId(), message.getChatId(), message.getMessage());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(savedMessage.getRecipientId()),
                "/queue/messages",
                ChatNotificationResponse.builder()
                        .id(savedMessage.getMessageId())
                        .senderId(savedMessage.getSenderId())
                        .recipientId(savedMessage.getRecipientId())
                        .message(savedMessage.getMessage())
                        .mediaUrls(savedMessage.getMediaUrls())
                        .sentAt(savedMessage.getSentAt())
                        .senderVisibility(savedMessage.getSenderVisibility())
                        .recipientVisibility(savedMessage.getRecipientVisibility())
                        .replyToMessageId(savedMessage.getReplyToMessageId())
                        .replyToSenderUsername(savedMessage.getReplyToSenderUsername())
                        .replyToSnippet(savedMessage.getReplyToSnippet())
                        .build());
    }

    @Override
    public void markMessagesAsRead(Long senderId, Long recipientId) {
        chatRoomService.getChatRoomId(senderId, recipientId, false).ifPresent(chatId -> {
            List<ChatMessageEntity> unreadMessages = chatMessageRepository.findByChatIdAndRecipientIdAndIsReadFalse(chatId, senderId);
            if (!unreadMessages.isEmpty()) {
                unreadMessages.forEach(msg -> msg.setIsRead(true));
                chatMessageRepository.saveAll(unreadMessages);
                
                // Notify the original sender that their messages have been read
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipientId),
                        "/queue/read-receipt",
                        Map.of("chatId", chatId, "readBy", senderId)
                );
            }
        });
    }
}
