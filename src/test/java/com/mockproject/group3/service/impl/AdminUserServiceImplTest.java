package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.admin.user.ChangeUserStatusRequest;
import com.mockproject.group3.dto.admin.user.CreateUserRequest;
import com.mockproject.group3.dto.admin.user.UpdateUserRequest;
import com.mockproject.group3.dto.admin.user.UserDetailResponse;
import com.mockproject.group3.dto.admin.user.UserListResponse;
import com.mockproject.group3.dto.common.PageResponse;
import com.mockproject.group3.entity.Facility;
import com.mockproject.group3.entity.Role;
import com.mockproject.group3.entity.User;
import com.mockproject.group3.entity.UserFacility;
import com.mockproject.group3.repository.FacilityRepository;
import com.mockproject.group3.repository.RoleRepository;
import com.mockproject.group3.repository.UserFacilityRepository;
import com.mockproject.group3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private UserFacilityRepository userFacilityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    void getUsers_ReturnsPageResponse() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("Admin");

        User user = new User();
        user.setId(1L);
        user.setEmployeeCode("EMP001");
        user.setEmail("test@test.com");
        user.setRole(role);

        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.searchUsers(null, null, null, null, PageRequest.of(0, 10)))
                .thenReturn(userPage);

        // Act
        PageResponse<UserListResponse> result = adminUserService.getUsers(null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getEmployeeCode()).isEqualTo("EMP001");
        assertThat(result.getPage().getTotalItems()).isEqualTo(1);
    }

    @Test
    void createUser_Success() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeCode("EMP002")
                .email("new@test.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .roleId(1L)
                .build();

        Role role = new Role();
        role.setId(1L);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmployeeCode("EMP002");
        savedUser.setEmail("new@test.com");
        savedUser.setRole(role);

        when(userRepository.existsByEmployeeCode("EMP002")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userFacilityRepository.findByUserId(2L)).thenReturn(Collections.emptyList());

        // Act
        UserDetailResponse response = adminUserService.createUser(request);

        // Assert
        assertThat(response.getEmployeeCode()).isEqualTo("EMP002");
        assertThat(response.getEmail()).isEqualTo("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeCode("EMP003")
                .email("duplicate@test.com")
                .build();

        when(userRepository.existsByEmployeeCode("EMP003")).thenReturn(false);
        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateUserById_Success() {
        // Arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("John");
        existingUser.setRole(new Role());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userFacilityRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        UserDetailResponse response = adminUserService.updateUserById(1L, request);

        // Assert
        assertThat(response.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void changeUserStatus_Success() {
        // Arrange
        ChangeUserStatusRequest request = new ChangeUserStatusRequest();
        request.setStatus("INACTIVE");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setStatus("ACTIVE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userFacilityRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        UserDetailResponse response = adminUserService.changeUserStatus(1L, request);

        // Assert
        assertThat(response.getStatus()).isEqualTo("INACTIVE");
    }
}
