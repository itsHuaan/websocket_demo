package com.example.websocket_demo.service.auth.impl;

import com.example.websocket_demo.security.domain.UserDetailsImpl;
import com.example.websocket_demo.security.jwt.JwtProvider;
import com.example.websocket_demo.dto.response.SignInResponse;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.enumeration.AuthValidation;
import com.example.websocket_demo.mapper.UserMapper;
import com.example.websocket_demo.dto.request.EmailRequest;
import com.example.websocket_demo.dto.request.SignInRequest;
import com.example.websocket_demo.dto.request.SignUpRequest;
import com.example.websocket_demo.repository.IUserRepository;
import com.example.websocket_demo.service.auth.IAuthService;
import com.example.websocket_demo.service.email.IEmailService;
import com.example.websocket_demo.service.otp.IOtpService;
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
    UserMapper userMapper;
    IEmailService emailService;
    SpringTemplateEngine templateEngine;
    IOtpService otpService;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${app.otp.expires-minutes}")
    int OTP_EXPIRES_MINUTES;

    @Override
    public SignInResponse signIn(SignInRequest credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserEntity user = userDetails.getUser();
        String jwt = jwtProvider.generateTokenByUsername(user.getUsername());
        return new SignInResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                jwt,
                user.getProfilePicture());
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


