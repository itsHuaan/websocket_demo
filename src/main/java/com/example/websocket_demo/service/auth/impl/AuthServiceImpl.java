package com.example.websocket_demo.service.auth.impl;

import com.example.websocket_demo.configuration.UserDetailsImpl;
import com.example.websocket_demo.configuration.jwt.JwtProvider;
import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.SignInResponse;
import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.enumeration.AuthValidation;
import com.example.websocket_demo.mapper.UserMapper;
import com.example.websocket_demo.model.EmailModel;
import com.example.websocket_demo.model.SignInRequest;
import com.example.websocket_demo.model.SignUpRequest;
import com.example.websocket_demo.repository.IUserRepository;
import com.example.websocket_demo.service.auth.IAuthService;
import com.example.websocket_demo.service.email.IEmailService;
import com.example.websocket_demo.service.otp.IOtpService;
import com.example.websocket_demo.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class AuthServiceImpl implements IAuthService {
    JwtProvider jwtProvider;
    AuthenticationManager authenticationManager;
    IUserRepository userRepository;
    UserMapper userMapper;
    IEmailService emailService;
    SpringTemplateEngine templateEngine;
    IOtpService otpService;

    @Override
    public ApiResponse<?> signIn(SignInRequest credentials) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, AuthValidation.BAD_CREDENTIAL.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtProvider.generateTokenByUsername(userDetails.getUsername());
        return new ApiResponse<>(HttpStatus.OK, "You're now logged in", new SignInResponse(
                userDetails.getUser().getUserId(),
                userDetails.getUsername(),
                jwt));
    }

    @Override
    public ApiResponse<?> signUp(SignUpRequest credentials) {
        String email = credentials.getEmail();
        boolean isExistingByEmail = userRepository.existsByEmailAndDeletedAtIsNull(email);
        boolean isExistingByUsername = userRepository.existsByUsernameAndDeletedAtIsNull(credentials.getUsername());
        if (isExistingByEmail && isExistingByUsername) {
            return new ApiResponse<>(HttpStatus.OK, "User is existing", null);
        } else if (isExistingByEmail) {
            return new ApiResponse<>(HttpStatus.OK, AuthValidation.USER_EXISTING_BY_EMAIL.getMessage(), null);
        } else if (isExistingByUsername) {
            return new ApiResponse<>(HttpStatus.OK, AuthValidation.USER_EXISTING_BY_USERNAME.getMessage(), null);
        }
        try {
            UserDto user = userMapper.toUserDto(userRepository.save(userMapper.toUserEntity(credentials)));
            EmailModel emailModel = new EmailModel(email, "OTP", getEmailContent(otpService.generateAndStoreOtp(email).getData(), 3));
            emailService.sendEmail(emailModel);
            return new ApiResponse<>(HttpStatus.OK, "An activation code was sent to email " + user.getEmail(), null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    private String getEmailContent(String otp, int expireMinute){
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expireMinute", expireMinute);
        return templateEngine.process("otp-code", context);
    }
}
