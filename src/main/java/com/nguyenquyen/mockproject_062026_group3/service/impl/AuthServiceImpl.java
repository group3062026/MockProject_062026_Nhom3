package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.request.LoginRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResetPasswordRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.LoginResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ResetPasswordResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.User;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import com.nguyenquyen.mockproject_062026_group3.security.JwtTokenProvider;
import com.nguyenquyen.mockproject_062026_group3.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Authenticate via Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Retrieve user to get roles, name and update lastLoginAt
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        // Generate token
        String token = jwtTokenProvider.generateAccessToken(authentication);
        String sessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);
        String name = user.getFirstName() + " " + user.getLastName();

        return LoginResponse.builder()
                .token(token)
                .sessionId(sessionId)
                .role(user.getRole().getRoleName())
                .name(name)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        // Find user by email
        boolean exists = userRepository.findByEmailAndIsDeletedFalse(request.getEmail()).isPresent();
        if (!exists) {
            // According to API document, return 404 Not Found if email doesn't exist
            throw new com.nguyenquyen.mockproject_062026_group3.exception.ResourceNotFoundException("User", "email", request.getEmail()); 
        }

        // Logic to send reset password email goes here (out of scope for this task)
        log.info("Sending password reset link to: {}", request.getEmail());

        return new ResetPasswordResponse("Password reset link sent to email");
    }
}
