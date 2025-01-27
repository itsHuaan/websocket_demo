package com.example.websocket_demo.service.role.impl;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.model.RoleModel;
import com.example.websocket_demo.service.role.IRoleActionService;
import com.example.websocket_demo.service.role.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements IRoleService {
    IRoleActionService roleActionService;

    @Override
    public ApiResponse<?> addRole(RoleModel role) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Failed to add role";
        try {
            if (roleActionService.addRole(role) == 1) {
                status = HttpStatus.OK;
                message = "Role added";
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new ApiResponse<>(status, message, null);
    }

    @Override
    public ApiResponse<?> updateRole(Long id, String roleName) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Failed to update role";
        if (roleName == null || roleName.isEmpty()) {
            message = "Role name cannot be empty";
        }
        try {
            if (roleActionService.updateRole(new RoleEntity(id, roleName)) == 1) {
                status = HttpStatus.OK;
                message = "Role updated";
            }
        } catch (NoSuchElementException e) {
            message = e.getMessage();
        }
        return new ApiResponse<>(status, message, null);
    }
}
