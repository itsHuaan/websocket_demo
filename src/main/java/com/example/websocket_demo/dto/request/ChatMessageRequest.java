package com.example.websocket_demo.dto.request;

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
@ToString
public class ChatMessageRequest {
    Long id;
    String chatId;
    Long senderId;
    Long recipientId;
    String message;
    List<String> mediaUrls;
    Integer senderVisibility;
    Integer recipientVisibility;
    Long replyToMessageId;
}

