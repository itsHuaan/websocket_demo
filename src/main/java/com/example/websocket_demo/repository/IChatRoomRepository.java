package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ChatRoomEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IChatRoomRepository extends JpaRepository<ChatRoomEntity, Long>, JpaSpecificationExecutor<ChatRoomEntity> {
    ChatRoomEntity findChatRoomEntityByChatId(@NotNull String chatId);
}
