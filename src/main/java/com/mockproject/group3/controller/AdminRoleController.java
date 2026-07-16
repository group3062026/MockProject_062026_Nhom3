package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.dto.admin.role.RoleResponse;
import com.mockproject.group3.service.AdminRoleService;
import com.mockproject.group3.common.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    public ApiResponse<List<RoleResponse>> getRoleList() {
        return ApiResponse.success(adminRoleService.getRoleList());
    }
}
