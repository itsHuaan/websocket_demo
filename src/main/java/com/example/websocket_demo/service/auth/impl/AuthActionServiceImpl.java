package com.example.websocket_demo.service.auth.impl;

import com.example.websocket_demo.configuration.UserDetailsImpl;
import com.example.websocket_demo.configuration.jwt.JwtProvider;
import com.example.websocket_demo.dto.SignInResponse;
import com.example.websocket_demo.enumeration.AuthValidation;
import com.example.websocket_demo.model.SignInRequest;
import com.example.websocket_demo.repository.ITokenBlackListRepository;
import com.example.websocket_demo.service.auth.IAuthActionService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.*;
import org. springframework. security. core. Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class AuthActionServiceImpl implements IAuthActionService {
    JwtProvider jwtProvider;
    AuthenticationManager authenticationManager;
    ITokenBlackListRepository tokenBlacklistRepository;

    @Override
    public SignInResponse signIn(SignInRequest credentials) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            throw new SecurityException(AuthValidation.BAD_CREDENTIAL.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during authentication");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtProvider.generateTokenByUsername(userDetails.getUsername());
        return new SignInResponse(
                userDetails.getUser().getUserId(),
                userDetails.getUsername(),
                jwt
        );
    }
}
