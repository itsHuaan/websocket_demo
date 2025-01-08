package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_test")
public class TestEntity extends BaseEntity{
    @OneToMany(mappedBy = "testRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<TestMediaEntity> testMedia;
}
