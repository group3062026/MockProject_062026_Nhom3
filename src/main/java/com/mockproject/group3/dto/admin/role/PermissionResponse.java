package com.mockproject.group3.dto.admin.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for permission within a role.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionResponse {
    private Long id;
    private String actionCode;
    private Boolean isPhiSensitive;
}
