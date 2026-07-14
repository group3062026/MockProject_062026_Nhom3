package com.mockproject.group3.dto.carelevelhistory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCareLevelHistoryRequest {

    private Long careLevelId;

    private LocalDate startDate;

    @NotBlank(message = "Reason is required for editing history records")
    private String reason;
}

