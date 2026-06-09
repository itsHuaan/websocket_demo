package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.request.ChatMessageRequest;
import com.example.websocket_demo.dto.response.ChatHistoryResponse;
import com.example.websocket_demo.dto.response.ChatMessageResponse;
import com.example.websocket_demo.dto.response.ChatRoomResponse;
import com.example.websocket_demo.entity.ChatMediaEntity;
import com.example.websocket_demo.entity.ChatMessageEntity;
import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.repository.IChatMessageRepository;
import com.example.websocket_demo.repository.IUserRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ChatMapper {
    @Autowired
    protected IUserRepository userRepository;
    @Autowired
    protected IChatMessageRepository chatMessageRepository;

    @Mapping(target = "sender", expression = "java(getUsername(chatRoomEntity.getSenderId()))")
    @Mapping(target = "recipient", expression = "java(getUsername(chatRoomEntity.getRecipientId()))")
    @Mapping(target = "messages", expression = "java(getMessages(chatRoomEntity))")
    public abstract ChatRoomResponse toChatRoomDto(ChatRoomEntity chatRoomEntity);

    @Mapping(target = "chatMedias", ignore = true)
    @Mapping(target = "chatMessageId", ignore = true)
    public abstract ChatMessageEntity toChatMessageEntity(ChatMessageRequest chatMessageRequest);

    @AfterMapping
    protected void linkChatMedias(ChatMessageRequest chatMessageRequest, @MappingTarget ChatMessageEntity chatMessage) {
        if (chatMessageRequest.getMediaUrls() != null) {
            List<ChatMediaEntity> chatMedias = chatMessageRequest.getMediaUrls().stream()
                    .map(url -> ChatMediaEntity.builder()
                            .mediaUrl(url)
                            .chatMessage(chatMessage)
                            .build())
                    .toList();
            chatMessage.setChatMedias(chatMedias);
        }
    }

    @Mapping(target = "messageId", source = "chatMessageId")
    @Mapping(target = "senderUsername", expression = "java(getUsername(chatMessageEntity.getSenderId()))")
    @Mapping(target = "recipientUsername", expression = "java(getUsername(chatMessageEntity.getRecipientId()))")
    @Mapping(target = "mediaUrls", expression = "java(getMediaUrls(chatMessageEntity))")
    @Mapping(target = "sentAt", source = "createdAt")
    @Mapping(target = "replyToSenderUsername", expression = "java(getUsername(chatMessageEntity.getReplyToSenderId()))")
    public abstract ChatMessageResponse toChatMessageDto(ChatMessageEntity chatMessageEntity);

    @Mapping(target = "sender", expression = "java(getUsername(chatMessageEntity.getSenderId()))")
    @Mapping(target = "profilePicture", expression = "java(getProfilePicture(chatMessageEntity.getSenderId()))")
    @Mapping(target = "sentAt", source = "createdAt")
    public abstract ChatHistoryResponse toChatHistoryDto(ChatMessageEntity chatMessageEntity);

    @Named("getUsername")
    protected String getUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User not found with id: " + userId)).getUsername();
    }

    @Named("getProfilePicture")
    protected String getProfilePicture(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User not found with id: " + userId)).getProfilePicture();
    }

    protected List<ChatHistoryResponse> getMessages(ChatRoomEntity chatRoomEntity) {
        String chatId = chatRoomEntity.getChatId();
        return chatMessageRepository.findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(chatId).stream()
                .map(this::toChatHistoryDto)
                .toList();
    }

    protected List<String> getMediaUrls(ChatMessageEntity entity) {
        if (entity.getChatMedias() == null) return Collections.emptyList();
        return entity.getChatMedias().stream()
                .map(ChatMediaEntity::getMediaUrl)
                .toList();
    }
}
