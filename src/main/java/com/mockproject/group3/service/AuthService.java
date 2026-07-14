package com.mockproject.group3.service;

import com.mockproject.group3.dto.auth.LoginRequest;
import com.mockproject.group3.dto.auth.LoginResponse;
import com.mockproject.group3.dto.auth.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}

