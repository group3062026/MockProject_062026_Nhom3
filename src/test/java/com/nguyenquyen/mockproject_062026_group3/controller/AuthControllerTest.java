package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.LoginRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResetPasswordRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.LoginResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ResetPasswordResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.GlobalExceptionHandler;
import com.nguyenquyen.mockproject_062026_group3.exception.ResourceNotFoundException;
import com.nguyenquyen.mockproject_062026_group3.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Standalone setup kèm GlobalExceptionHandler để test cả error response
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // =====================================================================
    // POST /api/auth/login
    // =====================================================================

    @Test
    void login_withValidCredentials_shouldReturn200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "Nhms@Demo2026");
        LoginResponse loginResponse = LoginResponse.builder()
                .token("jwt-token-abc123")
                .sessionId("sess_8f2e1c")
                .role("System_Administrator")
                .name("Daniel Brooks")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-abc123"))
                .andExpect(jsonPath("$.data.sessionId").value("sess_8f2e1c"))
                .andExpect(jsonPath("$.data.role").value("System_Administrator"))
                .andExpect(jsonPath("$.data.name").value("Daniel Brooks"));
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_withBlankUsername_shouldReturn400ValidationError() throws Exception {
        LoginRequest request = new LoginRequest("", "Nhms@Demo2026");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.username").value("Username is required"));
    }

    @Test
    void login_withBlankPassword_shouldReturn400ValidationError() throws Exception {
        LoginRequest request = new LoginRequest("daniel.brooks@nhms-demo.local", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.password").value("Password is required"));
    }

    @Test
    void login_withAllFieldsBlank_shouldReturn400WithMultipleErrors() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    void login_withNullBody_shouldReturn500() throws Exception {
        // GlobalExceptionHandler không xử lý riêng HttpMessageNotReadableException
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // =====================================================================
    // POST /api/auth/reset-password
    // =====================================================================

    @Test
    void resetPassword_withExistingEmail_shouldReturn200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("daniel.brooks@nhms-demo.local");
        ResetPasswordResponse response = new ResetPasswordResponse("Password reset link sent to email");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.message").value("Password reset link sent to email"));
    }

    @Test
    void resetPassword_withNonExistentEmail_shouldReturn404() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("nonexistent@nhms-demo.local");

        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenThrow(new ResourceNotFoundException("User", "email", "nonexistent@nhms-demo.local"));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resetPassword_withBlankEmail_shouldReturn400ValidationError() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void resetPassword_withInvalidEmailFormat_shouldReturn400ValidationError() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("not-an-email");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.data.email").value("Invalid email format"));
    }

    @Test
    void resetPassword_withNullBody_shouldReturn500() throws Exception {
        // GlobalExceptionHandler không xử lý riêng HttpMessageNotReadableException
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
