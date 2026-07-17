package com.nguyenquyen.mockproject_062026_group3.dto.admin.incident;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new severity level.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateIncidentSeverityRequest {

    @NotBlank(message = "Level name is required")
    private String levelName;

    private Boolean chartLockTrigger;
}
