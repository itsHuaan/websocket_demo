package com.example.websocket_demo.service.user.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.security.domain.UserDetailsImpl;
import com.example.websocket_demo.security.identifier.UserIdentifierResolver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailServiceImpl implements UserDetailsService {

    /** Ordered by {@code @Order}: username first, then email, then future methods. */
    List<UserIdentifierResolver> resolvers;

    /**
     * Loads a user by any supported sign-in identifier (username, email, …).
     * The parameter is named {@code username} only to satisfy the Spring
     * Security contract; it may be any identifier the user typed.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(identifier))
                .map(resolver -> resolver.resolve(identifier))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with identifier: " + identifier));
    }

}
