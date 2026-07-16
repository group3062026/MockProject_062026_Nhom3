package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.role.RoleResponse;

import java.util.List;

public interface AdminRoleService {
    List<RoleResponse> getRoleList();
}
