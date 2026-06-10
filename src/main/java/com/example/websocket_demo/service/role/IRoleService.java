package com.example.websocket_demo.service.role;

import com.example.websocket_demo.dto.request.RoleRequest;
import com.example.websocket_demo.dto.response.RoleResponse;

import java.util.List;

public interface IRoleService {
    List<RoleResponse> getAllRoles();
    void addRole(RoleRequest role);
    void updateRole(Long id, String roleName);
}


