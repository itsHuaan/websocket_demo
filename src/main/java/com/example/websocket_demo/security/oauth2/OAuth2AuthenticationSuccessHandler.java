package com.example.websocket_demo.security.oauth2;

import com.example.websocket_demo.entity.RefreshTokenEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.repository.RefreshTokenRepository;
import com.example.websocket_demo.repository.UserRepository;
import com.example.websocket_demo.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;

import com.example.websocket_demo.security.oauth2.userinfo.OAuth2UserInfo;
import com.example.websocket_demo.security.oauth2.userinfo.OAuth2UserInfoFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MessageService messageService;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        if (email == null && authentication instanceof OAuth2AuthenticationToken) {
            String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
            
            String id = oAuth2UserInfo.getId();
            if (id != null) {
                email = registrationId + "_" + id + "@" + registrationId + ".local";
            }
        }

        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage(USER_NOT_FOUND.getCode())));

        String jwt = jwtProvider.generateTokenByUsername(user.getUsername());
        
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .userId(user.getUserId())
                .expiresAt(LocalDateTime.now().plus(REFRESH_EXPIRATION, ChronoUnit.MILLIS))
                .build();
        String refreshToken = refreshTokenRepository.save(entity).getToken();

        String targetUrl = "/?token=" + jwt + "&refreshToken=" + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
