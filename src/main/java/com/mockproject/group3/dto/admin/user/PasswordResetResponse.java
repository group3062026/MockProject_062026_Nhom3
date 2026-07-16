package com.mockproject.group3.dto.admin.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for simulated password reset.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordResetResponse {
    private Long userId;
    private String simulatedResetLink;
    private OffsetDateTime expiresAt;
}
