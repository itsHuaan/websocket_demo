package com.example.websocket_demo.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageModel {
    Long id;
    String chatId;
    Long senderId;
    Long recipientId;
    String message;
    List<String> mediaUrls;
    Integer senderVisibility;
    Integer recipientVisibility;
}
