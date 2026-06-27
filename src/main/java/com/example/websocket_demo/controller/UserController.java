package com.example.websocket_demo.controller;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.UserRequest;
import com.example.websocket_demo.dto.request.AdminUserRequest;
import com.example.websocket_demo.service.user.UserService;
import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.validation.PageableValidation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;

@RestController
@Tag(name = "User Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    MessageService messageService;
    UserService userManagementService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getUser(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(USERS_FETCHED.getCode()), userManagementService.getAllUsers(PageableValidation.setDefault(page, size))));
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(USER_FETCHED.getCode()), userManagementService.getUserById(id)));
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/u/{username}")
    public ResponseEntity<ApiResponse<?>> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(USER_FETCHED.getCode()), userManagementService.getUserByUsername(username)));
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody UserRequest UserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, messageService.getMessage(USER_CREATED_SUCCESSFULLY.getCode()), userManagementService.createUser(UserRequest)));
    }

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, messageService.getMessage(UNAUTHORIZED.getCode())));
        }
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PROFILE_FETCHED.getCode()), userManagementService.getUserById(userId)));
    }

    @Operation(summary = "Update my own profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateMyProfile(UserRequest userRequest, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED, messageService.getMessage(UNAUTHORIZED.getCode())));
        }
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PROFILE_UPDATED.getCode()), userManagementService.updateProfile(userId, userRequest)));
    }

    @Operation(summary = "Update an user (admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable Long id,
                                        @RequestBody AdminUserRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(USER_UPDATED_SUCCESSFULLY.getCode()), userManagementService.adminUpdateUser(id, request)));
    }

    @Operation(summary = "Delete an user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id,
                                        @RequestParam(required = false) Integer isHardDelete) {
        userManagementService.deleteUser(id, isHardDelete);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(USER_DELETED.getCode())));
    }

    @Operation(summary = "Get online users")
    @GetMapping("/online")
    public ResponseEntity<ApiResponse<?>> getOnlineUsers() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(ONLINE_USERS_FETCHED.getCode()), userManagementService.getOnlineUsers()));
    }
}

