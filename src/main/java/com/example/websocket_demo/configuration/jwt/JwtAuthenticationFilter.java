package com.example.websocket_demo.configuration.jwt;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.websocket_demo.configuration.UserDetailsImpl;
import com.example.websocket_demo.repository.ITokenBlackListRepository;
import com.example.websocket_demo.service.impl.UserServiceImpl;

import java.io.IOException;

@Log4j2
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserServiceImpl userServiceImpl;
    private final ITokenBlackListRepository tokenBlackListRepository;
    @Value("${jwt.secret-key}")
    private String JWT_SECRET;

    @Autowired
    public JwtAuthenticationFilter(JwtProvider jwtProvider, 
            UserServiceImpl userServiceImpl,
            ITokenBlackListRepository tokenBlackListRepository) {
        this.jwtProvider = jwtProvider;
        this.userServiceImpl = userServiceImpl;
        this.tokenBlackListRepository = tokenBlackListRepository;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwtToken = getJwtFromRequest(request);
            if (jwtToken != null && this.validateToken(jwtToken) && !tokenBlackListRepository.existsByToken(jwtToken)) {
                String email = jwtProvider.getKeyByValueFromJWT("email", jwtToken);
                UserDetailsImpl userDetails = (UserDetailsImpl) userServiceImpl.loadUserByUsername(email);
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }
            }
        } catch (Exception e) {
            logger.error("e: ", e);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}
