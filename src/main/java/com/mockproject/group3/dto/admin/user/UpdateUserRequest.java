package com.mockproject.group3.dto.admin.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating an existing user.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String phoneNumber;
    private Long roleId;
    private List<UserFacilityDto> facilities;
}
