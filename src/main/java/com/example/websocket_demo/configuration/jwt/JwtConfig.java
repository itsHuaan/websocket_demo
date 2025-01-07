package com.example.websocket_demo.configuration.jwt;

import com.example.websocket_demo.service.user.impl.UserDetailServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.websocket_demo.repository.ITokenBlackListRepository;

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
