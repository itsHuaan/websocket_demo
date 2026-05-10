package com.example.websocket_demo.service.chat;

import com.example.websocket_demo.dto.response.ChatRoomResponse;
import com.example.websocket_demo.dto.response.ChatUserResponse;

import java.util.List;
import java.util.Optional;

public interface IChatRoomService {
    Optional<String> getChatRoomId(Long senderId, Long recipientId, boolean createIfNotExist);

    ChatRoomResponse getChatRoom(Long senderId, Long recipientId);

    List<ChatUserResponse> getChatUsers(Long senderId);
}

