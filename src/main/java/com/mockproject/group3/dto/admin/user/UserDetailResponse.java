package com.mockproject.group3.dto.admin.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Detailed response DTO for a single user (getUserByID).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponse {
    private Long id;
    private String employeeCode;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String licenseNumber;
    private String phoneNumber;
    private UserListResponse.RoleDto role;
    private String status;
    private Boolean mfaEnabled;
    private List<UserFacilityDto> facilities;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
