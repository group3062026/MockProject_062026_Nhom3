package com.mockproject.group3.dto.carelevelhistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelHistoryResponse {
    private Long id;
    private Long careLevelId;
    private String levelCode;
    private LocalDate startDate;
    private LocalDate endDate;
}

