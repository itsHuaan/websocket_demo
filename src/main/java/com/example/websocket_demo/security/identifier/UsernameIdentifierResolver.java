package com.example.websocket_demo.security.identifier;

import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.repository.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves a sign-in identifier as a username. Tried first: every authenticated
 * request re-loads the principal by its real username (carried in the JWT), so
 * this path must stay exact and fast.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsernameIdentifierResolver implements UserIdentifierResolver {

    IUserRepository userRepository;

    @Override
    public boolean supports(String identifier) {
        return identifier != null && !identifier.isBlank();
    }

    @Override
    public Optional<UserEntity> resolve(String identifier) {
        return userRepository.findByUsernameAndDeletedAtIsNull(identifier);
    }
}
