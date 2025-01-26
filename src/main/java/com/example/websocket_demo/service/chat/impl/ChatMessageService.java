package com.example.websocket_demo.service.chat.impl;

import com.example.websocket_demo.dto.ChatMessageDto;
import com.example.websocket_demo.entity.ChatMessageEntity;
import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.mapper.ChatMapper;
import com.example.websocket_demo.model.ChatMessageModel;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService implements IChatMessageService {
    IChatMessageRepository chatMessageRepository;
    ChatMapper chatMapper;
    IChatRoomService chatRoomService;
    IChatRoomRepository chatRoomRepository;

    @Override
    public ChatMessageDto getChatMessageById(Long id) {
        return chatMapper.toChatMessageDto(Objects.requireNonNull(chatMessageRepository.findById(id).orElse(null)));
    }

    @Override
    public int saveMessage(ChatMessageModel message) {
        if (Objects.equals(message.getSenderId(), message.getRecipientId())) {
            throw new IllegalArgumentException("You can't send messages to yourself");
        }
        var chatRoomId = chatRoomService.getChatRoomId(message.getSenderId(),
                message.getRecipientId(),
                true).orElseThrow(() -> new IllegalArgumentException("Can't find chat room"));
        message.setChatId(chatRoomId);
        ChatMessageEntity entity = chatMapper.toChatMessageEntity(message);
        chatMessageRepository.save(entity);
        message.setId(entity.getChatMessageId());
        return 1;
    }

    @Override
    public Page<ChatMessageDto> getChatMessages(Long senderId, Long recipientId, Pageable pageable) {
        return chatRoomService.getChatRoomId(senderId, recipientId, false)
                .map(chatId -> chatMessageRepository.findAllByChatIdOrderByCreatedAtDesc(chatId, pageable)
                        .map(chatMapper::toChatMessageDto))
                .orElseThrow(() -> new NoSuchElementException("Chat room not found"));
    }

    @Override
    public int deleteMessage(Long senderId, Long recipientId) {
        try {
            var chatRoomId = chatRoomService.getChatRoomId(senderId, recipientId, false).orElseThrow(() -> new IllegalArgumentException("Can't find chat room"));
            List<ChatRoomEntity> chatRooms = chatRoomRepository.findChatRoomEntitiesByChatId(chatRoomId);
            chatRoomRepository.deleteAll(chatRooms);
            List<ChatMessageEntity> chatMessages = chatMessageRepository.findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(chatRoomId);
            chatMessageRepository.deleteAll(chatMessages);
            return 1;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
}
