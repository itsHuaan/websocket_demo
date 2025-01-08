package com.example.websocket_demo.service.chat.impl;

import com.example.websocket_demo.dto.ChatRoomDto;
import com.example.websocket_demo.dto.ChatUserDto;
import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.mapper.ChatMapper;
import com.example.websocket_demo.repository.IChatRoomRepository;
import com.example.websocket_demo.repository.IUserRepository;
import com.example.websocket_demo.service.chat.IChatRoomService;
import com.example.websocket_demo.specification.ChatSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRoomService implements IChatRoomService {
    IChatRoomRepository chatRoomRepository;
    ChatMapper chatMapper;
    IUserRepository userRepository;

    @Override
    public Optional<String> getChatRoomId(Long senderId, Long recipientId, boolean createIfNotExist) {
        Long firstId = Math.min(senderId, recipientId);
        Long secondId = Math.max(senderId, recipientId);
        return chatRoomRepository.findOne(Specification.where(
                        ChatSpecification.hasNormalizedChat(firstId, secondId)
                ))
                .map(ChatRoomEntity::getChatId)
                .or(() -> {
                    if (createIfNotExist) {
                        try {
                            var chatId = createChatId(firstId, secondId);
                            return Optional.of(chatId);
                        } catch (NoSuchElementException e) {
                            log.error(e.getMessage());
                        }
                    }
                    return Optional.empty();
                });
    }

    @Override
    public ChatRoomDto getChatRoom(Long senderId, Long recipientId) {
        return chatMapper.toChatRoomDto(chatRoomRepository.findChatRoomEntityByChatId(getChatRoomId(senderId, recipientId, false).orElse(null)));
    }

    @Override
    public List<ChatUserDto> getChatUsers(Long senderId) {
        return List.of();
    }

    private String createChatId(Long senderId, Long recipientId) {
        Long _senderId = userRepository.findById(senderId).orElseThrow(
                () -> new NoSuchElementException("Sender not found")
        ).getId();
        Long _recipientId = userRepository.findById(recipientId).orElseThrow(
                () -> new NoSuchElementException("Recipient not found")
        ).getId();
        var chatId = String.format("%s_%s", Math.min(_senderId, _recipientId), Math.max(_senderId, _recipientId));
        ChatRoomEntity senderRecipient = ChatRoomEntity.builder()
                .chatId(chatId)
                .senderId(_senderId)
                .recipientId(_recipientId)
                .build();
        ChatRoomEntity recipientSender = ChatRoomEntity.builder()
                .chatId(chatId)
                .senderId(_recipientId)
                .recipientId(_senderId)
                .build();
        chatRoomRepository.saveAll(List.of(senderRecipient, recipientSender));
        return chatId;
    }
}
