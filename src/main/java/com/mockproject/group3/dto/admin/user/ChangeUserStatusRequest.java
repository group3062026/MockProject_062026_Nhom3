package com.mockproject.group3.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing user status (Activate/Deactivate/Unlock).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUserStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String reason;
}
