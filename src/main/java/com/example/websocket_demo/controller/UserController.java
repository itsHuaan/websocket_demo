package com.example.websocket_demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.UserModel;
import com.example.websocket_demo.service.user.IUserService;
import com.example.websocket_demo.util.Const;
import com.example.websocket_demo.validation.PageableValidation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@RestController
@Tag(name = "User Controller")
@RequestMapping(value = Const.API_PREFIX + "/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userManagementService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<?> getUser(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userManagementService.getAllUsers(PageableValidation.setDefault(page, size)));
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        ApiResponse<?> response = userManagementService.getUserById(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/u/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        ApiResponse<?> response = userManagementService.getUserByUsername(username);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserModel userModel) {
        ApiResponse<?> response = userManagementService.createUser(userModel);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Update an user")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        UserModel userModel) {
        ApiResponse<?> response = userManagementService.updateUser(id, userModel);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Delete an user")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestParam(required = false) Integer isHardDelete) {
        ApiResponse<?> response = userManagementService.deleteUser(id, isHardDelete);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
