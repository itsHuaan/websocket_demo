package com.example.websocket_demo.dto.response;

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
public class ChatNotificationResponse {
    Long id;
    Long senderId;
    Long recipientId;
    String message;
    List<String> mediaUrls;
    private LocalDateTime sentAt;
    Integer senderVisibility;
    Integer recipientVisibility;
}

