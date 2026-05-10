package com.example.websocket_demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageResponse {
    Long messageId;
    String chatId;
    Long senderId;
    String senderUsername;
    Long recipientId;
    String recipientUsername;
    String message;
    List<String> mediaUrls;
    LocalDateTime sentAt;
    Integer senderVisibility;
    Integer recipientVisibility;
}

