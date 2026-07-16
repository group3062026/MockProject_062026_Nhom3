package com.mockproject.group3.dto.admin.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for role with permissions list.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse {
    private Long id;
    private String roleName;
    private String description;
    private Boolean isDeleted;
    private List<PermissionResponse> permissions;
}
