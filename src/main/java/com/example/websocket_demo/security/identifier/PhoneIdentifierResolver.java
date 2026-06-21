package com.example.websocket_demo.security.identifier;

import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.enumeration.VietnamPhoneFormat;
import com.example.websocket_demo.repository.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves a sign-in identifier as a Vietnamese phone number. The typed value is
 * normalised to the same canonical form used at sign-up (VietnamPhoneFormat.ZERO),
 * so 0…, 84…, and +84… variants all resolve to the same account.
 */
@Component
@Order(3)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhoneIdentifierResolver implements UserIdentifierResolver {

    IUserRepository userRepository;

    @Override
    public boolean supports(String identifier) {
        return identifier != null && identifier.matches(Const.VN_PHONE_REGEX);
    }

    @Override
    public Optional<UserEntity> resolve(String identifier) {
        String canonical = DataUtil.formatVnPhone(identifier, VietnamPhoneFormat.ZERO);
        return userRepository.findByPhoneNumberAndDeletedAtIsNull(canonical);
    }
}
