package com.example.websocket_demo.service.role;

import com.example.websocket_demo.dto.request.RoleRequest;

public interface IRoleService {
    void addRole(RoleRequest role);
    void updateRole(Long id, String roleName);
}


