package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.LoginRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResetPasswordRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.LoginResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ResetPasswordResponse;
import com.nguyenquyen.mockproject_062026_group3.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResetPasswordResponse resetPasswordResponse = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(resetPasswordResponse));
    }
}
