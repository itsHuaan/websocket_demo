package com.example.websocket_demo.service.role.impl;

import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.model.RoleModel;
import com.example.websocket_demo.repository.IRoleRepository;
import com.example.websocket_demo.service.role.IRoleActionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleActionServiceImpl implements IRoleActionService {
    IRoleRepository roleRepository;

    @Override
    public int addRole(RoleModel role) {
        if (role.getRoleName() == null || role.getRoleName().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        String roleName = modifyRoleName(role.getRoleName().toUpperCase());
        if (roleRepository.findByRoleName(roleName).isPresent()) {
            throw new IllegalArgumentException("Role already exist");
        }
        try {
            roleRepository.save(RoleEntity.builder().roleName(roleName).build());
            return 1;
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    @Override
    public int updateRole(RoleEntity role) {
        RoleEntity roleEntity = roleRepository.findById(role.getRoleId()).orElseThrow(
                () -> new NoSuchElementException("Role does not exist")
        );
        roleEntity.setRoleName(modifyRoleName(role.getRoleName().toUpperCase()));
        try {
            roleRepository.save(roleEntity);
            return 1;
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    private String modifyRoleName(String name) {
        if (!name.contains("ROLE_")) {
            name = "ROLE_" + name;
        }
        return name;
    }
}
