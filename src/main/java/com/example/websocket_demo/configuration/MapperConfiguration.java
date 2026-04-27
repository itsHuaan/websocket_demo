package com.example.websocket_demo.configuration;

import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.model.SignUpRequest;
import com.example.websocket_demo.model.UserModel;
import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.common.DateUtil;
import com.example.websocket_demo.common.Mapper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MapperConfiguration {
    Mapper mapper;
    PasswordEncoder passwordEncoder;

    /**
     * Configures and centralizes all custom object mapping rules for the application's {@link Mapper}.
     * <p>
     * This method is executed <strong>once</strong> at application startup, thanks to the
     * {@code @PostConstruct} annotation. This approach guarantees that all mapping rules are
     * registered safely in a single-threaded environment before the application handles any
     * live requests.
     *
     * <h3>Developer "How-To"</h3>
     * <p>
     * To add a new custom mapping, add another {@code mapper.registerCustomizer(...)} call
     * inside this method.
     *
     * <h3>THREAD-SAFETY WARNING</h3>
     * <p>
     * This is the <strong>only</strong> safe place to register customizers. Do <strong>not</strong>
     * call {@code mapper.registerCustomizer()} at runtime (e.g., from a {@code @Service} or
     * {@code @RestController}). Doing so will modify the shared mapper's state while it's in use,
     * causing race conditions and unpredictable, non-thread-safe behavior.
     */
    @PostConstruct
    public void configureAllCustomizers() {
        // All custom mapping rules are defined here to ensure thread-safety.
        // Add other customizers here...
        // mapper.registerCustomizer(Product.class, ProductDto.class, ...);
        mapper.registerCustomizer(UserEntity.class, UserDto.class, (entity, dto) -> {
            dto.setStatus(entity.getStatus() == 0 ? "Active" : "Inactive");
            dto.setCreatedAt(DateUtil.formatDate(entity.getCreatedAt(), Const.DateFormat.HHmmss_MMMddyyyy));
            dto.setModifiedAt(DateUtil.formatDate(entity.getCreatedAt(), Const.DateFormat.HHmmss_MMMddyyyy));
            dto.setDeletedAt(DateUtil.formatDate(entity.getCreatedAt(), Const.DateFormat.HHmmss_MMMddyyyy));
        });

        mapper.registerCustomizer(SignUpRequest.class, UserEntity.class, (request, entity) -> {
            entity.setRole(RoleEntity.builder().roleId(2L).build());
        });

        mapper.registerCustomizer(UserModel.class, UserEntity.class, (model, entity) -> {
            entity.setPassword(passwordEncoder.encode(model.getPassword()));
            entity.setRole(model.getRoleId() == null
                    ? RoleEntity.builder().roleId(2L).build()
                    : RoleEntity.builder().roleId(model.getRoleId()).build());
        });
    }
}
