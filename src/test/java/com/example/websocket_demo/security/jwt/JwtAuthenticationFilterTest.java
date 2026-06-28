package com.example.websocket_demo.security.jwt;

import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.enumeration.AccountStatus;
import com.example.websocket_demo.repository.TokenBlackListRepository;
import com.example.websocket_demo.security.domain.UserDetailsImpl;
import com.example.websocket_demo.service.user.impl.UserDetailServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test (no Spring context / DB) for the per-request account-status check.
 * Before the fix, a deactivated user holding a still-valid token was authenticated;
 * the first test below would have failed.
 */
class JwtAuthenticationFilterTest {

    // Long alphanumeric (base64-safe) secret — jjwt base64-decodes the string form.
    private static final String SECRET =
            "thisIsAVeryLongSecretKeyForUnitTestsThatIsBase64SafeAndLongEnough1234567890ABCDEFGH";

    private JwtProvider jwtProvider;
    private UserDetailServiceImpl userDetailService;
    private TokenBlackListRepository blacklistRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtProvider = mock(JwtProvider.class);
        userDetailService = mock(UserDetailServiceImpl.class);
        blacklistRepository = mock(TokenBlackListRepository.class);
        filter = new JwtAuthenticationFilter(jwtProvider, userDetailService, blacklistRepository);
        ReflectionTestUtils.setField(filter, "JWT_SECRET", SECRET);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String validToken() {
        return Jwts.builder()
                .setSubject("5")
                .claim("username", "john")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    private UserEntity user(int status, RoleEntity role) {
        return UserEntity.builder()
                .userId(5L)
                .username("john")
                .email("john@example.com")
                .password("x")
                .status(status)
                .role(role)
                .build();
    }

    @Test
    void deactivatedUser_withValidToken_isNotAuthenticated() throws Exception {
        String token = validToken();
        // Give the inactive user a real role, so the ONLY thing that can stop
        // authentication is the status check (not an incidental NPE in getAuthorities()).
        RoleEntity role = RoleEntity.builder().roleId(2L).roleName("ROLE_USER").build();
        when(jwtProvider.getKeyByValueFromJWT(eq("username"), anyString())).thenReturn("john");
        when(blacklistRepository.existsByToken(anyString())).thenReturn(false);
        when(userDetailService.loadUserByUsername(anyString()))
                .thenReturn(new UserDetailsImpl(user(AccountStatus.INACTIVE.getValue(), role)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(any(), any()); // request proceeds → 401 downstream
    }

    @Test
    void activeUser_withValidToken_isAuthenticated() throws Exception {
        String token = validToken();
        RoleEntity role = RoleEntity.builder().roleId(2L).roleName("ROLE_USER").build();
        when(jwtProvider.getKeyByValueFromJWT(eq("username"), anyString())).thenReturn("john");
        when(blacklistRepository.existsByToken(anyString())).thenReturn(false);
        when(userDetailService.loadUserByUsername(anyString()))
                .thenReturn(new UserDetailsImpl(user(AccountStatus.ACTIVE.getValue(), role)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("5");
    }
}
