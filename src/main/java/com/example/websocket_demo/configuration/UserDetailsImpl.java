package com.example.websocket_demo.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import com.example.websocket_demo.entity.UserEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@Transactional
public class UserDetailsImpl implements UserDetails {

    private final UserEntity userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(userEntity.getRole().getRoleName()));
        return roles;
    }

    public UserEntity getUser() {
        return this.userEntity;
    }

    @Override
    public String getPassword() {
        return userEntity == null || userEntity.getPassword() == null ? null : userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity == null || userEntity.getUsername() == null ? null : userEntity.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getRoleName() {
        return userEntity.getRole().getRoleName();
    }
}
