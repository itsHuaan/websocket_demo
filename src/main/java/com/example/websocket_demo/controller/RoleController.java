package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.MediaUploadTestModel;
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

    @Operation(summary = "Add role")
    @PostMapping
    public ResponseEntity<?> addRole(RoleModel roleModel) {
        ApiResponse<?> response = roleService.addRole(roleModel);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Update role")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                        String roleName) {
        ApiResponse<?> response = roleService.updateRole(id, roleName);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
