package com.example.websocket_demo.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.model.UserModel;

public interface IUserActionService {
    Page<UserDto> getAllUsers(Pageable pageable);

    int createUser(UserModel userModel);

    int updateUser(Long id, UserModel userModel);
    
    UserDto getUserByUsername(String username);

    UserDto getUserById(Long id);

    int deleteUser(Long id, Integer isHardDelete);
}
