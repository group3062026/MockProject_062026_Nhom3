package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.auth.LoginRequest;
import com.mockproject.group3.dto.auth.LoginResponse;
import com.mockproject.group3.dto.auth.RefreshTokenRequest;
import com.mockproject.group3.entity.User;
import com.mockproject.group3.exception.UnauthorizedException;
import com.mockproject.group3.repository.UserRepository;
import com.mockproject.group3.security.JwtTokenProvider;
import com.mockproject.group3.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getEmail());

        // Update last login time
        User user = userRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        String fullName = buildFullName(user);

        log.info("User logged in successfully: {}", loginRequest.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .email(user.getEmail())
                .fullName(fullName)
                .role(user.getRole().getRoleName())
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String token = refreshTokenRequest.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .email(user.getEmail())
                .fullName(buildFullName(user))
                .role(user.getRole().getRoleName())
                .build();
    }

    private String buildFullName(User user) {
        StringBuilder sb = new StringBuilder(user.getFirstName());
        if (user.getMiddleName() != null && !user.getMiddleName().isBlank()) {
            sb.append(" ").append(user.getMiddleName());
        }
        sb.append(" ").append(user.getLastName());
        return sb.toString();
    }
}

