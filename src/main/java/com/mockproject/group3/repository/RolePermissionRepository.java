package com.mockproject.group3.repository;

import com.mockproject.group3.entity.RolePermission;
import com.mockproject.group3.entity.key.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}

