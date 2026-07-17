package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.role.PermissionResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.role.RoleResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.Role;
import com.nguyenquyen.mockproject_062026_group3.entity.RolePermission;
import com.nguyenquyen.mockproject_062026_group3.repository.RolePermissionRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.RoleRepository;
import com.nguyenquyen.mockproject_062026_group3.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl implements AdminRoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoleList() {
        List<Role> roles = roleRepository.findByIsDeletedFalse();
        return roles.stream().map(role -> {
            List<RolePermission> rps = rolePermissionRepository.findByRoleId(role.getId());
            List<PermissionResponse> permissions = rps.stream()
                    .map(rp -> PermissionResponse.builder()
                            .id(rp.getPermission().getId())
                            .actionCode(rp.getPermission().getActionCode())
                            .isPhiSensitive(rp.getPermission().getIsPhiSensitive())
                            .build())
                    .collect(Collectors.toList());

            return RoleResponse.builder()
                    .id(role.getId())
                    .roleName(role.getRoleName())
                    .description(role.getDescription())
                    .isDeleted(role.getIsDeleted())
                    .permissions(permissions)
                    .build();
        }).collect(Collectors.toList());
    }
}
