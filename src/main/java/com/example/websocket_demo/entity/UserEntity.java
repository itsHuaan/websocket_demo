package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_user")
@FieldNameConstants
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userId;

    @Column(unique = true, nullable = false)
    @NotNull
    String email;

    String firstName;

    String lastName;

    @Column(unique = true, nullable = false)
    @NotNull
    @Size(min = 3, max = 50)
    String username;

    // Optional. Stored in a single canonical form (VietnamPhoneFormat.ZERO, e.g. 0912345678)
    // so the same number typed as 0…, 84…, or +84… resolves to one account at sign-in.
    @Column(unique = true)
    String phoneNumber;

    @Column(nullable = false)
    @NotNull
    String password;

    String profilePicture;

    @Column(nullable = false)
    @NotNull
    int status;

    // Admin-supplied reason shown to the user when their account is locked
    // (deactivated/suspended). Cleared on reactivation. Not exposed in UserResponse.
    @Column(length = 500)
    String statusReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    RoleEntity role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductEntity> products;
}
