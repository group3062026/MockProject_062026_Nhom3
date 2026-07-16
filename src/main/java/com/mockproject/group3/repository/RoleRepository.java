package com.mockproject.group3.repository;

import com.mockproject.group3.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleNameAndIsDeletedFalse(String roleName);
    
    List<Role> findByIsDeletedFalse();
}

