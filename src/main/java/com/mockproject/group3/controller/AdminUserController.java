package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.dto.admin.user.ChangeUserStatusRequest;
import com.mockproject.group3.dto.admin.user.CreateUserRequest;
import com.mockproject.group3.dto.admin.user.PasswordResetResponse;
import com.mockproject.group3.dto.admin.user.UpdateUserRequest;
import com.mockproject.group3.dto.admin.user.UserDetailResponse;
import com.mockproject.group3.dto.admin.user.UserListResponse;
import com.mockproject.group3.dto.common.PageResponse;
import com.mockproject.group3.service.AdminUserService;
import com.mockproject.group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<UserListResponse>> getUsers(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return ApiResponse.success(adminUserService.getUsers(roleId, status, facilityId, search, pageable));
    }

    @PostMapping
    public ApiResponse<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(adminUserService.createUser(request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserDetailResponse> getUserById(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserDetailResponse> updateUserById(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(adminUserService.updateUserById(userId, request));
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<UserDetailResponse> changeUserStatus(@PathVariable Long userId, @Valid @RequestBody ChangeUserStatusRequest request) {
        return ApiResponse.success(adminUserService.changeUserStatus(userId, request));
    }

    @PostMapping("/{userId}/password-reset")
    public ApiResponse<PasswordResetResponse> resetUserPassword(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.resetUserPassword(userId));
    }
}
