package com.example.websocket_demo.service.user.impl;

import java.util.List;
import java.util.Optional;

import com.example.websocket_demo.common.MessageService;
import com.example.websocket_demo.configuration.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.security.domain.UserDetailsImpl;
import com.example.websocket_demo.security.identifier.UserIdentifierResolver;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static com.example.websocket_demo.enumeration.ResponseMessage.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailServiceImpl implements UserDetailsService {
    MessageService messageService;

    /**
     * Ordered by {@code @Order}: username first, then email, then future methods.
     */
    List<UserIdentifierResolver> resolvers;

    /**
     * Loads a user by any supported sign-in identifier (username, email, …).
     * The parameter is named {@code username} only to satisfy the Spring
     * Security contract; it may be any identifier the user typed.
     *
     * Results are cached in-memory (Caffeine) to avoid hitting the database
     * on every authenticated request. The cache is evicted when user data is
     * modified (see {@code UserServiceImpl}, {@code AuthServiceImpl}).
     */
    @Override
    @Cacheable(value = CacheConfig.USER_DETAILS_CACHE, key = "#identifier")
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(identifier))
                .map(resolver -> resolver.resolve(identifier))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException(messageService.getMessage(USER_IDENTIFIER_NOT_FOUND.getCode(), identifier)));
    }

}
