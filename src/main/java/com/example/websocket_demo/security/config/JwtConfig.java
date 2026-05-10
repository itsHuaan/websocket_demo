package com.example.websocket_demo.security.config;

import com.example.websocket_demo.security.jwt.JwtAuthenticationFilter;
import com.example.websocket_demo.security.jwt.JwtProvider;
import com.example.websocket_demo.service.user.impl.UserDetailServiceImpl;
import com.example.websocket_demo.repository.ITokenBlackListRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtConfig {
    JwtProvider jwtProvider;
    UserDetailServiceImpl userDetailService;
    ITokenBlackListRepository tokenBlacklistRepository;

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, userDetailService, tokenBlacklistRepository);
    }
}
