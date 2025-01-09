package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.ChatHistoryDto;
import com.example.websocket_demo.dto.ChatMessageDto;
import com.example.websocket_demo.dto.ChatRoomDto;
import com.example.websocket_demo.entity.ChatMediaEntity;
import com.example.websocket_demo.entity.ChatMessageEntity;
import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.model.ChatMessageModel;
import com.example.websocket_demo.repository.IChatMessageRepository;
import com.example.websocket_demo.repository.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMapper {
    IUserRepository userRepository;
    IChatMessageRepository chatMessageRepository;

    public ChatRoomDto toChatRoomDto(ChatRoomEntity chatRoomEntity) {
        return ChatRoomDto.builder()
                .chatId(chatRoomEntity.getChatId())
                .sender(userRepository.findById(chatRoomEntity.getSenderId()).orElseThrow(
                        () -> new NoSuchElementException("User not found")).getUsername())
                .recipient(userRepository.findById(chatRoomEntity.getRecipientId()).orElseThrow(
                        () -> new NoSuchElementException("User not found")).getUsername())
                .messages(chatMessageRepository.findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(String.format("%s_%s", chatRoomEntity.getSenderId(), chatRoomEntity.getRecipientId())).stream()
                        .map(this::chatHistoryDto)
                        .toList())
                .build();
    }

    public ChatMessageEntity toChatMessageEntity(ChatMessageModel chatMessageModel) {
        ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                .chatId(chatMessageModel.getChatId())
                .senderId(chatMessageModel.getSenderId())
                .recipientId(chatMessageModel.getRecipientId())
                .message(chatMessageModel.getMessage())
                .senderVisibility(chatMessageModel.getSenderVisibility())
                .recipientVisibility(chatMessageModel.getRecipientVisibility())
                .build();
        if (chatMessageModel.getMediaUrls() != null) {
            List<ChatMediaEntity> chatMedias = chatMessageModel.getMediaUrls().stream()
                    .map(url -> ChatMediaEntity.builder()
                            .mediaUrl(url)
                            .chatMessage(chatMessage)
                            .build())
                    .toList();
            chatMessage.setChatMedias(chatMedias);
        } else {
            chatMessage.setChatMedias(null);
        }
        return chatMessage;
    }

    public ChatMessageDto toChatMessageDto(ChatMessageEntity chatMessageEntity) {
        UserEntity sender = userRepository.findById(chatMessageEntity.getSenderId()).orElseThrow(
                () -> new NoSuchElementException("Sender not found")
        );
        UserEntity recipient = userRepository.findById(chatMessageEntity.getRecipientId()).orElseThrow(
                () -> new NoSuchElementException("Recipient not found")
        );

        return ChatMessageDto.builder()
                .messageId(chatMessageEntity.getId())
                .chatId(chatMessageEntity.getChatId())
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .recipientId(recipient.getId())
                .recipientUsername(recipient.getUsername())
                .message(chatMessageEntity.getMessage())
                .mediaUrls(chatMessageEntity.getChatMedias().stream()
                        .map(ChatMediaEntity::getMediaUrl)
                        .toList())
                .sentAt(chatMessageEntity.getCreatedAt())
                .build();
    }

    public ChatHistoryDto chatHistoryDto(ChatMessageEntity chatMessageEntity) {
        UserEntity sender = userRepository.findById(chatMessageEntity.getSenderId()).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        return ChatHistoryDto.builder()
                .sender(sender.getUsername())
                .profilePicture(sender.getProfilePicture())
                .message(chatMessageEntity.getMessage())
                .sentAt(chatMessageEntity.getCreatedAt())
                .build();
    }
}
