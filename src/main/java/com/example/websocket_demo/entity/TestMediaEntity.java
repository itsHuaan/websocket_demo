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
@Table(name = "tbl_test_media")
public class TestMediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long testMediaId;
    String mediaUrl;
    @ManyToOne
    @JoinColumn(name = "test_id")
    TestEntity testRecord;
}
