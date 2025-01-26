package com.example.websocket_demo.entity;

import java.util.List;

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
@Table(name = "tbl_chat_message")
public class ChatMessageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long chatMessageId;

    @NotNull
    String chatId;

    @NotNull
    Long senderId;

    @NotNull
    Long recipientId;

    String message;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<ChatMediaEntity> chatMedias;

    @Column(nullable = false, columnDefinition = "INT default 1")
    Integer senderVisibility = 1;

    @Column(nullable = false, columnDefinition = "INT default 1")
    Integer recipientVisibility = 1;
}
