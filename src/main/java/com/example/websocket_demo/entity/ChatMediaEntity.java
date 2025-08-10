package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chat_media")
@FieldNameConstants
public class ChatMediaEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long chatMediaId;

    String mediaUrl;

    @ManyToOne
    @JoinColumn(name="chat_message_id")
    ChatMessageEntity chatMessage;
}
