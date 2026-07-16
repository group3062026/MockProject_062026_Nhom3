package com.mockproject.group3.dto.admin.carelevel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for care level.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CareLevelResponse {
    private Long id;
    private String levelCode;
    private String levelName;
    private Boolean isDeleted;
}
