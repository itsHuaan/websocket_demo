package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ChatMessageEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long>, JpaSpecificationExecutor<ChatMessageEntity> {
    ChatMessageEntity findChatMessageEntityByChatIdOrderByCreatedAtDesc(@NotNull String chatId);
    List<ChatMessageEntity> findChatMessageEntitiesByChatIdOrderByCreatedAtDesc(String chatId);

    Page<ChatMessageEntity> findAllByChatIdOrderByCreatedAtDesc(@NotNull String chatId, Pageable pageable);

    // Messages still visible to the given user — excludes those they removed for
    // themselves (their side's visibility flag is 0). Deleted-for-everyone rows
    // stay (visibility is untouched) so the tombstone is shown.
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.chatId = :chatId AND " +
            "((m.senderId = :userId AND m.senderVisibility = 1) OR " +
            "(m.recipientId = :userId AND m.recipientVisibility = 1)) " +
            "ORDER BY m.createdAt DESC")
    Page<ChatMessageEntity> findVisibleMessages(@Param("chatId") String chatId, @Param("userId") Long userId, Pageable pageable);

    List<ChatMessageEntity> findByChatIdAndRecipientIdAndIsReadFalse(String chatId, Long recipientId);
}
