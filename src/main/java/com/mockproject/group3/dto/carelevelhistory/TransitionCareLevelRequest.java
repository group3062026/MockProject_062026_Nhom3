package com.mockproject.group3.dto.carelevelhistory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransitionCareLevelRequest {

    @NotNull(message = "Care level ID is required")
    private Long careLevelId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}

