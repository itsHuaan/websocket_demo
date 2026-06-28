package com.example.websocket_demo.security.oauth2;

import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.enumeration.AccountStatus;
import com.example.websocket_demo.repository.RoleRepository;
import com.example.websocket_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        
        String givenName = oAuth2User.getAttribute("given_name");
        if (givenName == null) givenName = oAuth2User.getAttribute("first_name");
        
        String familyName = oAuth2User.getAttribute("family_name");
        if (familyName == null) familyName = oAuth2User.getAttribute("last_name");
        
        String name = oAuth2User.getAttribute("name");
        
        String picture = null;
        Object picObj = oAuth2User.getAttribute("picture");
        if (picObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> pictureObj = (java.util.Map<String, Object>) picObj;
            if (pictureObj.containsKey("data")) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> dataObj = (java.util.Map<String, Object>) pictureObj.get("data");
                if (dataObj.containsKey("url")) {
                    picture = (String) dataObj.get("url");
                }
            }
        } else if (picObj instanceof String) {
            picture = (String) picObj;
        }

        if (email == null) {
            String id = oAuth2User.getAttribute("id");
            if (id == null) {
                id = oAuth2User.getAttribute("sub"); // Fallback for Google
            }
            if (id != null) {
                String registrationId = userRequest.getClientRegistration().getRegistrationId();
                email = registrationId + "_" + id + "@" + registrationId + ".local";
            } else {
                throw new OAuth2AuthenticationException(messageService.getMessage(OAUTH2_EMAIL_NOT_FOUND.getCode()));
            }
        }

        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        UserEntity user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            boolean changed = false;
            if (user.getDeletedAt() != null) {
                user.setDeletedAt(null);
                user.setStatus(AccountStatus.ACTIVE.getValue());
                user.setStatusReason(null);
                changed = true;
            }
            if (picture != null && !picture.equals(user.getProfilePicture())) {
                user.setProfilePicture(picture);
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        } else {
            RoleEntity role = roleRepository.findByRoleName("ROLE_USER").orElseGet(() -> {
                RoleEntity newRole = new RoleEntity();
                newRole.setRoleName("ROLE_USER");
                newRole.setDescription("User role");
                return roleRepository.save(newRole);
            });

            String username = email.split("@")[0];
            // ensure username is unique
            if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
                username = username + "_" + UUID.randomUUID().toString().substring(0, 5);
            }

            user = UserEntity.builder()
                    .email(email)
                    .username(username)
                    .firstName(givenName != null ? givenName : name)
                    .lastName(familyName)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .status(AccountStatus.ACTIVE.getValue())
                    .role(role)
                    .profilePicture(picture)
                    .build();
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
