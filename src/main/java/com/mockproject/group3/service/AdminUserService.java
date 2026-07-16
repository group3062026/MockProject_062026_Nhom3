package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.user.ChangeUserStatusRequest;
import com.mockproject.group3.dto.admin.user.CreateUserRequest;
import com.mockproject.group3.dto.admin.user.PasswordResetResponse;
import com.mockproject.group3.dto.admin.user.UpdateUserRequest;
import com.mockproject.group3.dto.admin.user.UserDetailResponse;
import com.mockproject.group3.dto.admin.user.UserListResponse;
import com.mockproject.group3.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    PageResponse<UserListResponse> getUsers(Long roleId, String status, Long facilityId, String search, Pageable pageable);

    UserDetailResponse createUser(CreateUserRequest request);

    UserDetailResponse getUserById(Long userId);

    UserDetailResponse updateUserById(Long userId, UpdateUserRequest request);

    UserDetailResponse changeUserStatus(Long userId, ChangeUserStatusRequest request);

    PasswordResetResponse resetUserPassword(Long userId);
}
