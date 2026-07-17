package com.nguyenquyen.mockproject_062026_group3.dto.admin.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for user list items (compact).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserListResponse {
    private Long id;
    private String employeeCode;
    private String email;
    private String firstName;
    private String lastName;
    private RoleDto role;
    private String status;
    private OffsetDateTime lastLoginAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleDto {
        private Long id;
        private String roleName;
    }
}
