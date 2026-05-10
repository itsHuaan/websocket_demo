package com.example.websocket_demo.service.role.impl;

import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.dto.request.RoleRequest;
import com.example.websocket_demo.repository.IRoleRepository;
import com.example.websocket_demo.service.role.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements IRoleService {
    IRoleRepository roleRepository;

    @Override
    public void addRole(RoleRequest role) {
        if (role.getRoleName() == null || role.getRoleName().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        String roleName = modifyRoleName(role.getRoleName().toUpperCase());
        if (roleRepository.findByRoleName(roleName).isPresent()) {
            throw new IllegalArgumentException("Role already exist");
        }
        roleRepository.save(RoleEntity.builder().roleName(roleName).build());
    }

    @Override
    public void updateRole(Long id, String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        RoleEntity roleEntity = roleRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Role does not exist")
        );
        roleEntity.setRoleName(modifyRoleName(roleName.toUpperCase()));
        roleRepository.save(roleEntity);
    }

    private String modifyRoleName(String name) {
        if (!name.contains("ROLE_")) {
            name = "ROLE_" + name;
        }
        return name;
    }
}


