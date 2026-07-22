package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.LoginRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResetPasswordRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.LoginResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ResetPasswordResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.Role;
import com.nguyenquyen.mockproject_062026_group3.entity.User;
import com.nguyenquyen.mockproject_062026_group3.exception.ResourceNotFoundException;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import com.nguyenquyen.mockproject_062026_group3.security.JwtTokenProvider;
import com.nguyenquyen.mockproject_062026_group3.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .roleName("System_Administrator")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("daniel.brooks@nhms-demo.local")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Daniel")
                .lastName("Brooks")
                .status("ACTIVE")
                .role(testRole)
                .isDeleted(false)
                .build();
    }

    // =====================================================================
    // Login Tests
    // =====================================================================

    @Test
    void login_withValidCredentials_shouldReturnTokenAndUserInfo() {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "Nhms@Demo2026");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(authentication))
                .thenReturn("jwt-token-abc123");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-abc123", response.getToken());
        assertEquals("System_Administrator", response.getRole());
        assertEquals("Daniel Brooks", response.getName());
        assertNotNull(response.getSessionId());
        assertTrue(response.getSessionId().startsWith("sess_"));
    }

    @Test
    void login_shouldUpdateLastLoginAt() {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "Nhms@Demo2026");
        Authentication authentication = mock(Authentication.class);
        OffsetDateTime beforeLogin = OffsetDateTime.now().minusSeconds(1);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(authentication))
                .thenReturn("jwt-token-abc123");

        authService.login(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getLastLoginAt());
        assertTrue(savedUser.getLastLoginAt().isAfter(beforeLogin));
    }

    @Test
    void login_withInvalidPassword_shouldThrowBadCredentialsException() {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withNonExistentEmail_shouldThrowBadCredentialsException() {
        LoginRequest request = new LoginRequest("nonexistent@nhms-demo.local", "Password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, never()).findByEmailAndIsDeletedFalse(any());
    }

    @Test
    void login_shouldCallAuthenticationManagerWithCorrectCredentials() {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "Nhms@Demo2026");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(authentication))
                .thenReturn("jwt-token-abc123");

        authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertEquals("daniel.brooks@nhms-demo.local", authCaptor.getValue().getPrincipal());
        assertEquals("Nhms@Demo2026", authCaptor.getValue().getCredentials());
    }

    @Test
    void login_shouldGenerateUniqueSessionIds() {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "Nhms@Demo2026");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(authentication))
                .thenReturn("jwt-token-abc123");

        LoginResponse response1 = authService.login(request);
        LoginResponse response2 = authService.login(request);

        assertNotEquals(response1.getSessionId(), response2.getSessionId());
    }

    @Test
    void login_withUserHavingMiddleName_shouldReturnFirstAndLastNameOnly() {
        testUser.setMiddleName("James");
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "Nhms@Demo2026");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(authentication))
                .thenReturn("jwt-token-abc123");

        LoginResponse response = authService.login(request);

        // API contract chỉ yêu cầu name = firstName + lastName
        assertEquals("Daniel Brooks", response.getName());
    }

    // =====================================================================
    // Reset Password Tests
    // =====================================================================

    @Test
    void resetPassword_withExistingEmail_shouldReturnSuccessMessage() {
        ResetPasswordRequest request = new ResetPasswordRequest("daniel.brooks@nhms-demo.local");

        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));

        ResetPasswordResponse response = authService.resetPassword(request);

        assertNotNull(response);
        assertEquals("Password reset link sent to email", response.getMessage());
    }

    @Test
    void resetPassword_withNonExistentEmail_shouldThrowResourceNotFoundException() {
        ResetPasswordRequest request = new ResetPasswordRequest("nonexistent@nhms-demo.local");

        when(userRepository.findByEmailAndIsDeletedFalse("nonexistent@nhms-demo.local"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.resetPassword(request)
        );
        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    void resetPassword_withDeletedUser_shouldThrowResourceNotFoundException() {
        ResetPasswordRequest request = new ResetPasswordRequest("deleted@nhms-demo.local");

        // findByEmailAndIsDeletedFalse sẽ không tìm thấy user đã bị soft-delete
        when(userRepository.findByEmailAndIsDeletedFalse("deleted@nhms-demo.local"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_shouldNotModifyUserData() {
        ResetPasswordRequest request = new ResetPasswordRequest("daniel.brooks@nhms-demo.local");

        when(userRepository.findByEmailAndIsDeletedFalse("daniel.brooks@nhms-demo.local"))
                .thenReturn(Optional.of(testUser));

        authService.resetPassword(request);

        // Reset password chỉ kiểm tra email tồn tại, không được sửa dữ liệu user
        verify(userRepository, never()).save(any());
    }
}
