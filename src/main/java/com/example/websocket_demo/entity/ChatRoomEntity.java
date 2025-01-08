package com.example.websocket_demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chatroom")
public class ChatRoomEntity extends BaseEntity{
    @NotNull
    String chatId;

    @NotNull
    Long senderId;

    @NotNull
    Long recipientId;
}
