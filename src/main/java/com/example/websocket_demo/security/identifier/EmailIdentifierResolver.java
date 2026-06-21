package com.example.websocket_demo.security.identifier;

import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.repository.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves a sign-in identifier as an email address. Only consulted when the
 * identifier looks like an email, so it never shadows a plain username lookup.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailIdentifierResolver implements UserIdentifierResolver {

    IUserRepository userRepository;

    @Override
    public boolean supports(String identifier) {
        return identifier != null && identifier.matches(Const.EMAIL_REGEX);
    }

    @Override
    public Optional<UserEntity> resolve(String identifier) {
        return userRepository.findByEmailAndDeletedAtIsNull(identifier);
    }
}
