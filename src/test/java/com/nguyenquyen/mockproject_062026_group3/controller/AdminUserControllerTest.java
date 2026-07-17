package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.user.ChangeUserStatusRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.user.CreateUserRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.user.UpdateUserRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.user.UserDetailResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.user.UserListResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.common.PageResponse;
import com.nguyenquyen.mockproject_062026_group3.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.nguyenquyen.mockproject_062026_group3.config.SecurityConfig.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthenticationFilter.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthEntryPoint.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "NHA_Admin")
    void getUsers_ReturnsSuccess() throws Exception {
        UserListResponse mockResponse = UserListResponse.builder()
                .employeeCode("EMP001")
                .firstName("John")
                .lastName("Doe")
                .build();
                
        PageResponse<UserListResponse> pageResponse = PageResponse.<UserListResponse>builder()
                .data(List.of(mockResponse))
                .page(PageResponse.PageInfo.builder().page(1).pageSize(1).totalItems(1L).totalPages(1).build())
                .build();

        when(adminUserService.getUsers(any(), any(), any(), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].employeeCode").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "NHA_Admin")
    void createUser_ValidRequest_ReturnsSuccess() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeCode("EMP002")
                .email("test@example.com")
                .password("Password123")
                .firstName("Jane")
                .lastName("Doe")
                .roleId(1L)
                .build();

        UserDetailResponse mockResponse = UserDetailResponse.builder()
                .employeeCode("EMP002")
                .email("test@example.com")
                .build();

        when(adminUserService.createUser(any(CreateUserRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeCode").value("EMP002"));
    }

    @Test
    @WithMockUser(roles = "NHA_Admin")
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeCode("") // Invalid, should be NotBlank
                .email("invalid-email") // Invalid email format
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "NHA_Admin")
    void updateUserById_ReturnsSuccess() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("UpdatedName")
                .build();

        UserDetailResponse mockResponse = UserDetailResponse.builder()
                .firstName("UpdatedName")
                .build();

        when(adminUserService.updateUserById(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("UpdatedName"));
    }

    @Test
    @WithMockUser(roles = "NHA_Admin")
    void changeUserStatus_ReturnsSuccess() throws Exception {
        ChangeUserStatusRequest request = new ChangeUserStatusRequest();
        request.setStatus("INACTIVE");

        UserDetailResponse mockResponse = UserDetailResponse.builder()
                .status("INACTIVE")
                .build();

        when(adminUserService.changeUserStatus(eq(1L), any(ChangeUserStatusRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

}
