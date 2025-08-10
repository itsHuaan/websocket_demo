package com.example.websocket_demo.specification;

import com.example.websocket_demo.entity.ChatRoomEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class ChatSpecification {
    public static Specification<ChatRoomEntity> hasNormalizedChat(Long firstId, Long secondId) {
        return (Root<ChatRoomEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get(ChatRoomEntity.Fields.senderId), firstId),
                criteriaBuilder.equal(root.get(ChatRoomEntity.Fields.recipientId), secondId)
        );
    }
}
