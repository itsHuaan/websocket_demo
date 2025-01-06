package com.example.websocket_demo.configuration.jwt;

import com.example.websocket_demo.service.impl.UserDetailServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.websocket_demo.repository.ITokenBlackListRepository;
import com.example.websocket_demo.service.impl.UserActionServiceImpl;

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
