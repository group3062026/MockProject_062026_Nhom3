package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.LoginRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResetPasswordRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.LoginResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ResetPasswordResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    ResetPasswordResponse resetPassword(ResetPasswordRequest request);
}
