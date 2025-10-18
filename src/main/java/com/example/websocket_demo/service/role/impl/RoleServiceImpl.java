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
        try {
            return roleActionService.addRole(role) == 1
                    ? new ApiResponse<>(HttpStatus.OK, "Role added")
                    : new ApiResponse<>(HttpStatus.BAD_REQUEST, "Failed to add role");
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> updateRole(Long id, String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Role name cannot be empty");
        }
        try {
            if (roleActionService.updateRole(new RoleEntity(id, roleName)) == 1) {
                return new ApiResponse<>(HttpStatus.OK, "Role updated");
            }
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Failed to update role");
        } catch (NoSuchElementException e) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }
}
