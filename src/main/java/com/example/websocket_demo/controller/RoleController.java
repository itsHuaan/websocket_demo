package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.RoleRequest;
import com.example.websocket_demo.service.role.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.websocket_demo.common.Const;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@Tag(name = "Role Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    IRoleService roleService;

    @Operation(summary = "Add role", description = "'ROLE_' prefix in role name is optional")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addRole(RoleRequest RoleRequest) {
        roleService.addRole(RoleRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, "Role added"));
    }

    @Operation(summary = "Update role", description = "'ROLE_' prefix in role name is optional")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateRole(@PathVariable Long id,
                                        @RequestBody RoleRequest role) {
        roleService.updateRole(id, role.getRoleName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "Role updated"));
    }
}


