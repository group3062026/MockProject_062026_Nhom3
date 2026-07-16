package com.mockproject.group3.dto.admin.carelevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for enabling/disabling a care level.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCareLevelRequest {
    private Boolean isDeleted;
}
