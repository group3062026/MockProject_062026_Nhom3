package com.nguyenquyen.mockproject_062026_group3.dto.admin.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new user.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String licenseNumber;

    private String phoneNumber;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private List<UserFacilityDto> facilities;

    private String status;
}
