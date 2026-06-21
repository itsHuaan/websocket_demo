package com.example.websocket_demo.security.identifier;

import com.example.websocket_demo.entity.UserEntity;

import java.util.Optional;

/**
 * Resolves a free-form sign-in identifier (username, email, and in the future
 * phone number, etc.) to a user account.
 *
 * <p>Resolvers are consulted in {@link org.springframework.core.annotation.Order}
 * order during authentication; the first one that both {@link #supports(String)}
 * the identifier and {@link #resolve(String) resolves} it to a user wins. To add
 * a new sign-in method, add a new {@code @Component} implementing this interface —
 * no existing code needs to change.
 */
public interface UserIdentifierResolver {

    /** Whether this resolver can interpret the given identifier (cheap format check). */
    boolean supports(String identifier);

    /** Look up the user for this identifier, if one exists. */
    Optional<UserEntity> resolve(String identifier);
}
