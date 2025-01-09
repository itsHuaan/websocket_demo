package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ChatMessageEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IChatMessageRepository extends JpaRepository<ChatMessageEntity, Long>, JpaSpecificationExecutor<ChatMessageEntity> {
    ChatMessageEntity findChatMessageEntityByChatIdOrderByCreatedAtDesc(@NotNull String chatId);
    List<ChatMessageEntity> findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(String chatId);

    Page<ChatMessageEntity> findAllByChatIdOrderByCreatedAtDesc(@NotNull String chatId, Pageable pageable);
}
