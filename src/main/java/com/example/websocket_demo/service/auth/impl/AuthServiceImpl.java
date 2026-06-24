package com.example.websocket_demo.service.auth.impl;

import com.example.websocket_demo.security.domain.UserDetailsImpl;
import com.example.websocket_demo.security.jwt.JwtProvider;
import com.example.websocket_demo.dto.response.SignInResponse;
import com.example.websocket_demo.entity.RefreshTokenEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.enumeration.AuthValidation;
import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.enumeration.VietnamPhoneFormat;
import com.example.websocket_demo.mapper.UserMapper;
import com.example.websocket_demo.dto.request.EmailRequest;

import com.example.websocket_demo.dto.request.SignInRequest;
import com.example.websocket_demo.dto.request.SignUpRequest;
import com.example.websocket_demo.repository.IRefreshTokenRepository;
import com.example.websocket_demo.repository.IUserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import com.example.websocket_demo.service.auth.IAuthService;
import com.example.websocket_demo.service.email.IEmailService;
import com.example.websocket_demo.service.otp.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.websocket_demo.dto.request.VerifyOtpRequest;
import com.example.websocket_demo.enumeration.AccountStatus;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.websocket_demo.dto.request.ForgotPasswordRequest;
import com.example.websocket_demo.dto.request.ResetPasswordRequest;
import com.example.websocket_demo.dto.request.ResendOtpRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements IAuthService {
    JwtProvider jwtProvider;
    AuthenticationManager authenticationManager;
    IUserRepository userRepository;
    IRefreshTokenRepository refreshTokenRepository;
    UserMapper userMapper;
    IEmailService emailService;
    SpringTemplateEngine templateEngine;
    OtpService otpService;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${app.otp.expires-minutes}")
    int OTP_EXPIRES_MINUTES;

    @NonFinal
    @Value("${jwt.refresh-expiration}")
    long REFRESH_EXPIRATION;

    @Override
    public SignInResponse signIn(SignInRequest credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserEntity user = userDetails.getUser();
        String jwt = jwtProvider.generateTokenByUsername(user.getUsername());
        String refreshToken = createRefreshToken(user.getUserId());
        return new SignInResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                jwt,
                refreshToken,
                user.getProfilePicture(),
                user.getRole() != null ? user.getRole().getRoleName() : null);
    }

    @Override
    public SignInResponse refreshToken(String refreshToken) {
        RefreshTokenEntity stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("Refresh token has expired. Please sign in again.");
        }
        UserEntity user = userRepository.findById(stored.getUserId())
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // A deactivated/suspended account must not be able to mint a fresh token.
        // The refresh token is left intact so a later reactivation still works.
        if (user.getStatus() != AccountStatus.ACTIVE.getValue()) {
            String reason = user.getStatusReason();
            throw new IllegalArgumentException(reason != null && !reason.isBlank()
                    ? "Your account has been locked: " + reason
                    : "Your account is not active. Please contact an administrator.");
        }

        // Rotate: the used refresh token is single-use; issue a fresh access/refresh pair
        refreshTokenRepository.delete(stored);
        String newRefreshToken = createRefreshToken(user.getUserId());
        String jwt = jwtProvider.generateTokenByUsername(user.getUsername());

        return new SignInResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                jwt,
                newRefreshToken,
                user.getProfilePicture(),
                user.getRole() != null ? user.getRole().getRoleName() : null);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    private String createRefreshToken(Long userId) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiresAt(LocalDateTime.now().plus(REFRESH_EXPIRATION, ChronoUnit.MILLIS))
                .build();
        return refreshTokenRepository.save(entity).getToken();
    }

    @Override
    public void signUp(SignUpRequest credentials) {
        String email = credentials.getEmail();
        boolean isExistingByEmail = userRepository.existsByEmailAndDeletedAtIsNull(email);
        boolean isExistingByUsername = userRepository.existsByUsernameAndDeletedAtIsNull(credentials.getUsername());
        if (isExistingByEmail && isExistingByUsername) {
            throw new IllegalArgumentException("User already exists with both email and username");
        } else if (isExistingByEmail) {
            throw new IllegalArgumentException(AuthValidation.USER_EXISTING_BY_EMAIL.getMessage());
        } else if (isExistingByUsername) {
            throw new IllegalArgumentException(AuthValidation.USER_EXISTING_BY_USERNAME.getMessage());
        }

        // Phone number is optional. When supplied, validate, normalise to the canonical
        // form, and ensure it is not already taken; when blank, store null so multiple
        // accounts without a phone don't collide on the unique constraint.
        String phone = credentials.getPhoneNumber();
        if (phone != null && !phone.isBlank()) {
            if (!phone.matches(Const.VN_PHONE_REGEX)) {
                throw new IllegalArgumentException("Invalid Vietnamese phone number");
            }
            String canonicalPhone = DataUtil.formatVnPhone(phone, VietnamPhoneFormat.ZERO);
            if (userRepository.existsByPhoneNumberAndDeletedAtIsNull(canonicalPhone)) {
                throw new IllegalArgumentException("An account with this phone number already exists");
            }
            credentials.setPhoneNumber(canonicalPhone);
        } else {
            credentials.setPhoneNumber(null);
        }

        UserEntity user = userRepository.save(userMapper.toUserEntity(credentials));
        EmailRequest emailRequest = new EmailRequest(email, "Account Confirmation OTP", getEmailContent(otpService.generateAndStoreOtp(email).getData(), OTP_EXPIRES_MINUTES));
        emailService.sendEmail(emailRequest);
    }

    @Override
    public void resendSignUpOtp(ResendOtpRequest request) {
        String email = request.getEmail();
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));
        if (user.getStatus() == AccountStatus.ACTIVE.getValue()) {
            throw new IllegalArgumentException("This account is already verified. Please sign in.");
        }
        String otp = otpService.generateAndStoreOtp(email).getData();
        EmailRequest emailRequest = new EmailRequest(email, "Account Confirmation OTP", getEmailContent(otp, OTP_EXPIRES_MINUTES));
        emailService.sendEmail(emailRequest);
    }

    @Override
    public void verifySignUp(VerifyOtpRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String storedOtp = otpService.getOtp(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        if (otpService.isOtpUsed(email)) {
            throw new IllegalArgumentException("OTP has already been used");
        }
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(AccountStatus.ACTIVE.getValue());
        userRepository.save(user);
        otpService.markOtpAsUsed(email);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with this email"));
        
        String otp = otpService.generateAndStoreOtp(email).getData();
        EmailRequest emailRequest = new EmailRequest(email, "Reset Password OTP", getEmailContent(otp, OTP_EXPIRES_MINUTES));
        emailService.sendEmail(emailRequest);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String storedOtp = otpService.getOtp(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        if (otpService.isOtpUsed(email)) {
            throw new IllegalArgumentException("OTP has already been used");
        }
        
        UserEntity user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.markOtpAsUsed(email);
    }

    private String getEmailContent(String otp, int expiresMinute){
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiresMinutes", expiresMinute);
        return templateEngine.process("otp-code", context);
    }
}


