package com.example.websocket_demo.service.role;

import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.model.RoleModel;

public interface IRoleActionService {
    int addRole(RoleModel role);
    int updateRole(RoleEntity role);
}
