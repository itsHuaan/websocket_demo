package com.example.websocket_demo.mapper;

import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.common.DateUtil;
import com.example.websocket_demo.dto.request.SignUpRequest;
import com.example.websocket_demo.dto.request.UserRequest;
import com.example.websocket_demo.dto.response.UserResponse;
import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.repository.RoleRepository;
import com.example.websocket_demo.service.media.impl.CloudinaryServiceImpl;
import com.example.websocket_demo.enumeration.AccountStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected CloudinaryServiceImpl mediaUploader;
    @Autowired
    protected RoleRepository roleRepository;

    @Mapping(target = "status", expression = "java(mapStatus(userEntity.getStatus()))")
    @Mapping(target = "role", expression = "java(userEntity.getRole() != null ? userEntity.getRole().getRoleName() : null)")
    @Mapping(target = "createdAt", expression = "java(formatDate(userEntity.getCreatedAt()))")
    @Mapping(target = "modifiedAt", expression = "java(formatDate(userEntity.getModifiedAt()))")
    @Mapping(target = "deletedAt", expression = "java(formatDate(userEntity.getDeletedAt()))")
    public abstract UserResponse toUserDto(UserEntity userEntity);

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(userRequest.getPassword()))")
    @Mapping(target = "profilePicture", expression = "java(uploadProfilePicture(userRequest.getProfilePicture()))")
    @Mapping(target = "role", expression = "java(getRole(userRequest.getRoleId()))")
    @Mapping(target = "status", expression = "java(com.example.websocket_demo.enumeration.AccountStatus.ACTIVE.getValue())")
    public abstract UserEntity toUserEntity(UserRequest userRequest) throws IOException;

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(signUpRequest.getPassword()))")
    @Mapping(target = "status", expression = "java(com.example.websocket_demo.enumeration.AccountStatus.INACTIVE.getValue())")
    @Mapping(target = "role", expression = "java(getDefaultRole())")
    public abstract UserEntity toUserEntity(SignUpRequest signUpRequest);

    protected String mapStatus(int status) {
        if (status == AccountStatus.ACTIVE.getValue()) return AccountStatus.ACTIVE.getDescription();
        if (status == AccountStatus.INACTIVE.getValue()) return AccountStatus.INACTIVE.getDescription();
        if (status == AccountStatus.SUSPENDED.getValue()) return AccountStatus.SUSPENDED.getDescription();
        return AccountStatus.UNKNOWN.getDescription();
    }

    protected String formatDate(LocalDateTime date) {
        if (date == null) return null;
        return DateUtil.formatDate(date, Const.DateFormat.HHmmss_MMMddyyyy);
    }

    protected String uploadProfilePicture(MultipartFile file) throws IOException {
        return file != null ? mediaUploader.uploadMediaFile(file) : null;
    }

    protected RoleEntity getRole(Long roleId) {
        if (roleId != null) {
            return roleRepository.findById(roleId).orElseThrow(
                    () -> new NoSuchElementException("Role not found")
            );
        }
        return getDefaultRole();
    }

    protected RoleEntity getDefaultRole() {
        return RoleEntity.builder().roleId(2L).build();
    }
}
