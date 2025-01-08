package com.example.websocket_demo.service.chat;

import com.example.websocket_demo.dto.ChatRoomDto;
import com.example.websocket_demo.dto.ChatUserDto;

import java.util.List;
import java.util.Optional;

public interface IChatRoomService {
    Optional<String> getChatRoomId(Long senderId, Long recipientId, boolean createIfNotExist);

    ChatRoomDto getChatRoom(Long senderId, Long recipientId);

    List<ChatUserDto> getChatUsers(Long senderId);
}
