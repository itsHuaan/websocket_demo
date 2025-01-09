package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ChatRoomEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IChatRoomRepository extends JpaRepository<ChatRoomEntity, Long>, JpaSpecificationExecutor<ChatRoomEntity> {
    ChatRoomEntity findChatRoomEntityByChatId(@NotNull String chatId);

    List<ChatRoomEntity> findChatRoomEntitiesByChatId(@NotNull String chatId);
}
