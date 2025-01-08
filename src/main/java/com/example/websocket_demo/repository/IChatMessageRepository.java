package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IChatMessageRepository extends JpaRepository<ChatMessageEntity, Long>, JpaSpecificationExecutor<ChatMessageEntity> {
    List<ChatMessageEntity> findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(String chatId);
}
