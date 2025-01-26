package com.example.websocket_demo.service.role;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.RoleModel;

import javax.management.relation.Role;

public interface IRoleService {
    ApiResponse<?> addRole(RoleModel role);
    ApiResponse<?> updateRole(Long id, String roleName);
}
