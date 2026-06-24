package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.RoleRequest;
import com.example.websocket_demo.service.role.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.websocket_demo.common.Const;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@Tag(name = "Role Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    RoleService roleService;

    @Operation(summary = "Get all roles")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getRoles() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "Roles fetched", roleService.getAllRoles()));
    }

    @Operation(summary = "Add role", description = "'ROLE_' prefix in role name is optional")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addRole(@RequestBody RoleRequest RoleRequest) {
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


