package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chat_media")
public class ChatMediaEntity extends BaseEntity{
    String mediaUrl;
    @ManyToOne
    @JoinColumn(name="chat_message_id")
    ChatMessageEntity chatMessage;
}
