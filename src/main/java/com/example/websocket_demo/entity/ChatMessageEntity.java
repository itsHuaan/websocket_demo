package com.example.websocket_demo.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "tbl_chat_message")
@FieldNameConstants
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

    @Builder.Default
    @Column(nullable = false, columnDefinition = "INT default 1")
    Integer senderVisibility = 1;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "INT default 1")
    Integer recipientVisibility = 1;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN default false")
    Boolean isRead = false;

    // Reply metadata: a denormalized snapshot of the message being replied to.
    // Nullable — only set when this message is a reply.
    Long replyToMessageId;

    Long replyToSenderId;

    @Column(length = 160)
    String replyToSnippet;
}
