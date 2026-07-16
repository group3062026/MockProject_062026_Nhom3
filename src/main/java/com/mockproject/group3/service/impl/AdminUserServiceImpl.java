package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.admin.user.ChangeUserStatusRequest;
import com.mockproject.group3.dto.admin.user.CreateUserRequest;
import com.mockproject.group3.dto.admin.user.PasswordResetResponse;
import com.mockproject.group3.dto.admin.user.UpdateUserRequest;
import com.mockproject.group3.dto.admin.user.UserDetailResponse;
import com.mockproject.group3.dto.admin.user.UserFacilityDto;
import com.mockproject.group3.dto.admin.user.UserListResponse;
import com.mockproject.group3.dto.common.PageResponse;
import com.mockproject.group3.entity.Facility;
import com.mockproject.group3.entity.Role;
import com.mockproject.group3.entity.User;
import com.mockproject.group3.entity.UserFacility;
import com.mockproject.group3.entity.key.UserFacilityId;
import com.mockproject.group3.repository.FacilityRepository;
import com.mockproject.group3.repository.RoleRepository;
import com.mockproject.group3.repository.UserFacilityRepository;
import com.mockproject.group3.repository.UserRepository;
import com.mockproject.group3.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FacilityRepository facilityRepository;
    private final UserFacilityRepository userFacilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserListResponse> getUsers(Long roleId, String status, Long facilityId, String search, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(roleId, status, facilityId, search, pageable);
        Page<UserListResponse> mapped = users.map(user -> UserListResponse.builder()
                .id(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? new UserListResponse.RoleDto(user.getRole().getId(), user.getRole().getRoleName()) : null)
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .build());
        return PageResponse.of(mapped);
    }

    @Override
    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new RuntimeException("Employee code already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .employeeCode(request.getEmployeeCode())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .licenseNumber(request.getLicenseNumber())
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .isDeleted(false)
                .build();

        user = userRepository.save(user);

        saveUserFacilities(user, request.getFacilities());

        return mapToDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDetailResponse(user);
    }

    @Override
    @Transactional
    public UserDetailResponse updateUserById(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getLicenseNumber() != null) user.setLicenseNumber(request.getLicenseNumber());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }

        if (request.getFacilities() != null) {
            userFacilityRepository.deleteByUserId(user.getId());
            saveUserFacilities(user, request.getFacilities());
        }

        user = userRepository.save(user);
        return mapToDetailResponse(user);
    }

    @Override
    @Transactional
    public UserDetailResponse changeUserStatus(Long userId, ChangeUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(request.getStatus());
        // Could save reason if there's a field or audit log, ignoring for now
        user = userRepository.save(user);
        return mapToDetailResponse(user);
    }

    @Override
    public PasswordResetResponse resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Simulate password reset logic
        String token = UUID.randomUUID().toString();
        String resetLink = "https://example.com/reset-password?token=" + token;
        
        return PasswordResetResponse.builder()
                .userId(user.getId())
                .simulatedResetLink(resetLink)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();
    }

    private void saveUserFacilities(User user, List<UserFacilityDto> facilityDtos) {
        if (facilityDtos != null && !facilityDtos.isEmpty()) {
            for (UserFacilityDto fdto : facilityDtos) {
                Facility facility = facilityRepository.findById(fdto.getFacilityId())
                        .orElseThrow(() -> new RuntimeException("Facility not found: " + fdto.getFacilityId()));
                
                UserFacilityId id = new UserFacilityId(user.getId(), facility.getId());
                UserFacility uf = UserFacility.builder()
                        .id(id)
                        .user(user)
                        .facility(facility)
                        .isPrimary(fdto.getIsPrimary() != null ? fdto.getIsPrimary() : false)
                        .build();
                userFacilityRepository.save(uf);
            }
        }
    }

    private UserDetailResponse mapToDetailResponse(User user) {
        List<UserFacility> userFacilities = userFacilityRepository.findByUserId(user.getId());
        List<UserFacilityDto> facilities = userFacilities.stream()
                .map(uf -> new UserFacilityDto(uf.getFacility().getId(), uf.getIsPrimary()))
                .collect(Collectors.toList());

        return UserDetailResponse.builder()
                .id(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .licenseNumber(user.getLicenseNumber())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? new UserListResponse.RoleDto(user.getRole().getId(), user.getRole().getRoleName()) : null)
                .status(user.getStatus())
                .mfaEnabled(user.getMfaEnabled())
                .facilities(facilities)
                .isDeleted(user.getIsDeleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
