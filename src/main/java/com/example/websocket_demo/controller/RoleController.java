package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.RoleModel;
import com.example.websocket_demo.service.role.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.websocket_demo.util.Const;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@Tag(name = "Role Controller")
@RequestMapping(value = Const.API_PREFIX + "/role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    IRoleService roleService;

    @Operation(summary = "Add role", description = "'ROLE_' prefix in role name is optional")
    @PostMapping
    public ResponseEntity<?> addRole(RoleModel roleModel) {
        ApiResponse<?> response = roleService.addRole(roleModel);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Update role", description = "'ROLE_' prefix in role name is optional")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                        @RequestBody RoleModel role) {
        ApiResponse<?> response = roleService.updateRole(id, role.getRoleName());
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
